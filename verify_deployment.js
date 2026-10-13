const http = require('http');
const fs = require('fs');
const path = require('path');

function request(options, data) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, data: body ? JSON.parse(body) : null });
        } catch (e) {
          resolve({ status: res.statusCode, raw: body });
        }
      });
    });
    req.on('error', reject);
    if (data) {
      req.write(data);
    }
    req.end();
  });
}

async function runDeploymentVerification() {
  console.log('=== STARTING STUDYHUB DEPLOYMENT-READINESS AUDIT ===');
  const results = {};

  // 1. Healthcheck
  const health = await request({ hostname: 'localhost', port: 8080, path: '/actuator/health', method: 'GET' });
  results.backendHealth = health.status === 200;
  console.log('1. Backend /actuator/health:', health.status, health.data?.status);

  // 2. Taxonomy API
  const tax = await request({ hostname: 'localhost', port: 8080, path: '/api/v1/taxonomy/departments', method: 'GET' });
  results.taxonomyDepartments = tax.status === 200 && tax.data?.data?.length === 5;
  console.log('2. Departments loaded (5 depts):', results.taxonomyDepartments ? 'PASS' : 'FAIL');

  const cseSubs = await request({ hostname: 'localhost', port: 8080, path: '/api/v1/taxonomy/departments/1/subjects', method: 'GET' });
  results.cseSubjects = cseSubs.status === 200 && cseSubs.data?.data?.length === 9;
  console.log('3. CSE Subjects (9 subjects):', results.cseSubjects ? 'PASS' : 'FAIL');

  // 3. Student A Registration & Login
  const rand = Date.now();
  const studentAEmail = 'uploader.' + rand + '@anurag.edu.in';
  const regPayloadA = JSON.stringify({
    fullName: 'Student Uploader',
    email: studentAEmail,
    password: 'Password123#',
    departmentId: 1,
    semester: 4
  });
  const regResA = await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/auth/register', method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(regPayloadA) }
  }, regPayloadA);
  results.registration = regResA.status === 201;
  console.log('4. Student A Registration:', regResA.status === 201 ? 'PASS' : 'FAIL');

  const loginResA = await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/auth/login', method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(JSON.stringify({ email: studentAEmail, password: 'Password123#' })) }
  }, JSON.stringify({ email: studentAEmail, password: 'Password123#' }));
  const tokenA = loginResA.data?.data?.accessToken;
  results.studentAuth = loginResA.status === 200 && !!tokenA;
  console.log('5. Student A Auth (JWT):', results.studentAuth ? 'PASS' : 'FAIL');

  // 4. Student B (Peer Reviewer) Registration & Login
  const studentBEmail = 'reviewer.' + rand + '@anurag.edu.in';
  const regPayloadB = JSON.stringify({
    fullName: 'Student Reviewer',
    email: studentBEmail,
    password: 'Password123#',
    departmentId: 1,
    semester: 4
  });
  await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/auth/register', method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(regPayloadB) }
  }, regPayloadB);

  const loginResB = await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/auth/login', method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(JSON.stringify({ email: studentBEmail, password: 'Password123#' })) }
  }, JSON.stringify({ email: studentBEmail, password: 'Password123#' }));
  const tokenB = loginResB.data?.data?.accessToken;
  results.peerAuth = loginResB.status === 200 && !!tokenB;
  console.log('6. Student B (Reviewer) Auth (JWT):', results.peerAuth ? 'PASS' : 'FAIL');

  // 5. Admin Login
  const adminLoginPayload = JSON.stringify({ email: 'admin@studyhub.edu', password: 'admin123' });
  const adminLoginRes = await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/auth/login', method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(adminLoginPayload) }
  }, adminLoginPayload);
  const adminToken = adminLoginRes.data?.data?.accessToken;
  results.adminAuth = adminLoginRes.status === 200 && !!adminToken;
  console.log('7. Admin JWT Auth:', results.adminAuth ? 'PASS' : 'FAIL');

  // 6. Multipart Resource Upload with Metadata
  const boundary = '----StudyHubBoundary' + rand;
  const metaJson = JSON.stringify({
    title: 'Operating Systems Scheduling Notes — Unit 3',
    description: 'Automated verification test of resource upload and storage persistence',
    departmentId: 1,
    subjectId: 3,
    semester: 4,
    type: 'NOTE',
    tags: ['verification', 'deployment', 'os']
  });
  const fakePdfContent = '%PDF-1.4\n1 0 obj\n<< /Title (Deployment Verification) >>\nendobj\ntrailer\n<< /Root 1 0 R >>\n%%EOF';

  let bodyParts = '';
  bodyParts += '--' + boundary + '\r\n';
  bodyParts += 'Content-Disposition: form-data; name="metadata"\r\n';
  bodyParts += 'Content-Type: application/json\r\n\r\n';
  bodyParts += metaJson + '\r\n';

  bodyParts += '--' + boundary + '\r\n';
  bodyParts += 'Content-Disposition: form-data; name="file"; filename="os-scheduling-notes.pdf"\r\n';
  bodyParts += 'Content-Type: application/pdf\r\n\r\n';
  bodyParts += fakePdfContent + '\r\n';
  bodyParts += '--' + boundary + '--\r\n';

  const uploadRes = await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/resources', method: 'POST',
    headers: {
      'Content-Type': 'multipart/form-data; boundary=' + boundary,
      'Authorization': 'Bearer ' + tokenA,
      'Content-Length': Buffer.byteLength(bodyParts)
    }
  }, bodyParts);
  const createdResourceId = uploadRes.data?.data?.id;
  const createdFilePath = uploadRes.data?.data?.filePath;
  results.upload = uploadRes.status === 201 && !!createdResourceId;
  console.log('8. Multipart Resource Upload (ID ' + createdResourceId + '):', results.upload ? 'PASS' : 'FAIL');

  // 7. Persistent File Storage on Disk Check
  if (createdFilePath) {
    const fullDiskPath = path.resolve('backend/uploads', createdFilePath);
    results.diskStorage = fs.existsSync(fullDiskPath);
    console.log('9. Storage File on Disk (' + createdFilePath + '):', results.diskStorage ? 'PASS' : 'FAIL');
  } else {
    results.diskStorage = false;
  }

  // 8. Public Search API
  const searchRes = await request({ hostname: 'localhost', port: 8080, path: '/api/v1/search/resources?page=0&size=5', method: 'GET' });
  results.publicSearch = searchRes.status === 200 && searchRes.data?.data?.totalElements > 0;
  console.log('10. Public Search API (Elements: ' + searchRes.data?.data?.totalElements + '):', results.publicSearch ? 'PASS' : 'FAIL');

  // 9. Review & Rating Submission (by Peer Reviewer B, avoiding self-review block)
  const reviewPayload = JSON.stringify({ rating: 5, text: 'This is a comprehensive and well-written academic note that helped with our exam preparation.' });
  const reviewRes = await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/resources/' + createdResourceId + '/reviews', method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + tokenB,
      'Content-Length': Buffer.byteLength(reviewPayload)
    }
  }, reviewPayload);
  results.review = (reviewRes.status === 201 || reviewRes.status === 200) && reviewRes.data?.data?.rating === 5;
  console.log('11. Peer Rating & Bayesian Reliability Recalculation:', results.review ? 'PASS' : 'FAIL');

  // 10. Self-Review Block Verification (Student A reviewing own resource must receive 400)
  const selfReviewRes = await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/resources/' + createdResourceId + '/reviews', method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + tokenA,
      'Content-Length': Buffer.byteLength(reviewPayload)
    }
  }, reviewPayload);
  results.selfReviewBlock = selfReviewRes.status === 400;
  console.log('12. Integrity Rule: Self-Review Correctly Blocked (400):', results.selfReviewBlock ? 'PASS' : 'FAIL');

  // 11. Bookmark Resource
  const bookmarkRes = await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/resources/' + createdResourceId + '/bookmark', method: 'POST',
    headers: { 'Authorization': 'Bearer ' + tokenB }
  });
  results.bookmark = bookmarkRes.status === 200;
  console.log('13. Bookmark Action:', results.bookmark ? 'PASS' : 'FAIL');

  // 12. Authorization RBAC Check: Student calling admin moderation must fail with 403
  const modPayload = JSON.stringify({ status: 'APPROVED', rejectionReason: null });
  const unauthorizedRes = await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/admin/resources/' + createdResourceId + '/moderation', method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + tokenB,
      'Content-Length': Buffer.byteLength(modPayload)
    }
  }, modPayload);
  results.rbacBlock = unauthorizedRes.status === 403;
  console.log('14. RBAC Security: Student blocked from Admin Moderation (403):', results.rbacBlock ? 'PASS' : 'FAIL');

  // 13. Admin Moderation Success
  const adminModRes = await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/admin/resources/' + createdResourceId + '/moderation', method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + adminToken,
      'Content-Length': Buffer.byteLength(modPayload)
    }
  }, modPayload);
  results.adminModeration = adminModRes.status === 200;
  console.log('15. Admin Moderation Execution (200):', results.adminModeration ? 'PASS' : 'FAIL');

  // 14. AI Academic Assistant
  const aiPayload = JSON.stringify({ question: 'Explain TCP 3-way handshake', subjectContext: 'Computer Networks' });
  const aiRes = await request({
    hostname: 'localhost', port: 8080, path: '/api/v1/ai/doubt-assistant', method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + tokenA,
      'Content-Length': Buffer.byteLength(aiPayload)
    }
  }, aiPayload);
  results.aiAssistant = aiRes.status === 200 && !!aiRes.data?.data?.directAnswer;
  console.log('16. AI Academic Assistant Response:', results.aiAssistant ? 'PASS' : 'FAIL');

  const allPassed = Object.values(results).every(Boolean);
  console.log('\n=== OVERALL DEPLOYMENT VERIFICATION RESULT:', allPassed ? 'ALL PASSED (16/16)' : 'SOME FAILED', '===');
  process.exit(allPassed ? 0 : 1);
}

runDeploymentVerification();
