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
          resolve({ status: res.statusCode, headers: res.headers, data: body ? JSON.parse(body) : null });
        } catch (e) {
          resolve({ status: res.statusCode, headers: res.headers, raw: body });
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

async function runComprehensiveDebug() {
  console.log('====================================================');
  console.log('       STUDYHUB COMPLETE SYSTEM DEBUG AUDIT          ');
  console.log('====================================================\n');

  const tests = [];
  function assert(name, condition, details = '') {
    if (condition) {
      console.log(`[PASS] ${name}`);
      tests.push({ name, passed: true });
    } else {
      console.error(`[FAIL] ${name} ${details ? '- ' + details : ''}`);
      tests.push({ name, passed: false, details });
    }
  }

  try {
    // 1. Healthcheck
    const health = await request({ hostname: 'localhost', port: 8080, path: '/actuator/health', method: 'GET' });
    assert('1. Actuator Health is UP', health.status === 200 && health.data?.status === 'UP', `status=${health.status}`);

    // 2. Frontend Server Response
    const frontend = await request({ hostname: 'localhost', port: 3000, path: '/', method: 'GET' });
    assert('2. Frontend Dev Server (Port 3000) Serving HTML', frontend.status === 200 && typeof frontend.raw === 'string' && frontend.raw.includes('<div id="root">'), `status=${frontend.status}`);

    // 3. Taxonomy
    const depts = await request({ hostname: 'localhost', port: 8080, path: '/api/v1/taxonomy/departments', method: 'GET' });
    assert('3. Taxonomy: Fetch Departments', depts.status === 200 && depts.data?.data?.length > 0, `count=${depts.data?.data?.length}`);

    const subjects = await request({ hostname: 'localhost', port: 8080, path: '/api/v1/taxonomy/subjects?departmentId=1', method: 'GET' });
    assert('4. Taxonomy: Fetch Subjects for Department 1', subjects.status === 200 && subjects.data?.data?.length > 0, `count=${subjects.data?.data?.length}`);

    // 4. Student Auth Workflow
    const timestamp = Date.now();
    const studentEmail = `debug_student_${timestamp}@anurag.edu.in`;
    const password = 'Password123#';

    const regRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/auth/register', method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    }, JSON.stringify({
      fullName: 'Debug User Student',
      email: studentEmail,
      password: password,
      departmentId: 1,
      semester: 4
    }));
    assert('5. Auth: Register New Student', regRes.status === 201 && regRes.data?.data?.accessToken, `status=${regRes.status}`);
    const studentToken = regRes.data?.data?.accessToken;
    const studentRefreshToken = regRes.data?.data?.refreshToken;

    // Bad Credentials test
    const badLoginRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/auth/login', method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    }, JSON.stringify({ email: studentEmail, password: 'WrongPassword999' }));
    assert('6. Auth: Reject Invalid Password (401)', badLoginRes.status === 401, `status=${badLoginRes.status}`);

    // Token Refresh test
    const refreshRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/auth/refresh', method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    }, JSON.stringify({ refreshToken: studentRefreshToken }));
    assert('7. Auth: Refresh Token Workflow', refreshRes.status === 200 && refreshRes.data?.data?.accessToken, `status=${refreshRes.status}`);
    const currentStudentToken = refreshRes.data?.data?.accessToken || studentToken;

    // User Profile Me
    const meRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/users/me', method: 'GET',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    assert('8. User: Fetch Profile (/users/me)', meRes.status === 200 && meRes.data?.data?.email === studentEmail, `status=${meRes.status}`);

    // Admin Auth
    const adminLoginRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/auth/login', method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    }, JSON.stringify({ email: 'admin@studyhub.edu', password: 'admin123' }));
    assert('9. Auth: Admin Login', adminLoginRes.status === 200 && adminLoginRes.data?.data?.accessToken, `status=${adminLoginRes.status}`);
    const adminToken = adminLoginRes.data?.data?.accessToken;

    // 5. Peer Student (for reviews)
    const reviewerEmail = `debug_reviewer_${timestamp}@anurag.edu.in`;
    const regRevRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/auth/register', method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    }, JSON.stringify({
      fullName: 'Debug Reviewer',
      email: reviewerEmail,
      password: password,
      departmentId: 1,
      semester: 4
    }));
    const reviewerToken = regRevRes.data?.data?.accessToken;
    assert('10. Auth: Register Second Student for Peer Interactions', regRevRes.status === 201, `status=${regRevRes.status}`);

    // 6. Resource Upload & Retrieval
    const boundary = '----StudyHubDebugBoundary' + timestamp;
    const metaJson = JSON.stringify({
      title: 'Debug Operating System Architecture Notes',
      description: 'Comprehensive debugging suite test upload artifact',
      departmentId: 1,
      subjectId: 3,
      semester: 4,
      type: 'NOTE',
      tags: ['debug', 'architecture', 'os']
    });
    const fakePdfContent = '%PDF-1.4\n1 0 obj\n<< /Title (Debug Test) >>\nendobj\ntrailer\n<< /Root 1 0 R >>\n%%EOF';
    let bodyParts = '';
    bodyParts += '--' + boundary + '\r\nContent-Disposition: form-data; name="metadata"\r\nContent-Type: application/json\r\n\r\n' + metaJson + '\r\n';
    bodyParts += '--' + boundary + '\r\nContent-Disposition: form-data; name="file"; filename="debug-os-notes.pdf"\r\nContent-Type: application/pdf\r\n\r\n' + fakePdfContent + '\r\n';
    bodyParts += '--' + boundary + '--\r\n';

    const uploadRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/resources', method: 'POST',
      headers: {
        'Content-Type': 'multipart/form-data; boundary=' + boundary,
        'Authorization': 'Bearer ' + currentStudentToken,
        'Content-Length': Buffer.byteLength(bodyParts)
      }
    }, bodyParts);
    assert('11. Resource: Multipart Upload with PDF', uploadRes.status === 201 && uploadRes.data?.data?.id, `status=${uploadRes.status}`);
    const resourceId = uploadRes.data?.data?.id;

    // Fetch Resource Details
    const resDetail = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/resources/${resourceId}`, method: 'GET'
    });
    assert('12. Resource: Fetch Details Publicly', resDetail.status === 200 && resDetail.data?.data?.id === resourceId, `status=${resDetail.status}`);

    // Resource Download
    const downloadRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/resources/${resourceId}/download`, method: 'GET',
      headers: { 'Authorization': `Bearer ${reviewerToken}` }
    });
    assert('13. Resource: Download Endpoint Streams Content', downloadRes.status === 200, `status=${downloadRes.status}`);

    // Useful vote
    const voteRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/resources/${resourceId}/useful`, method: 'POST',
      headers: { 'Authorization': `Bearer ${reviewerToken}` }
    });
    assert('14. Resource: Toggle Useful Vote', voteRes.status === 200, `status=${voteRes.status}`);

    // Bookmark
    const bmRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/resources/${resourceId}/bookmark`, method: 'POST',
      headers: { 'Authorization': `Bearer ${reviewerToken}` }
    });
    assert('15. Resource: Toggle Bookmark', bmRes.status === 200, `status=${bmRes.status}`);

    // Submit Review by Peer
    const revPayload = JSON.stringify({ rating: 5, text: 'Clear diagrams and very well structured notes.' });
    const revRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/resources/${resourceId}/reviews`, method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${reviewerToken}` }
    }, revPayload);
    assert('16. Resource: Peer Review & Rating', revRes.status === 201 || revRes.status === 200, `status=${revRes.status}`);

    // Self-Review Block
    const selfRevRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/resources/${resourceId}/reviews`, method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${currentStudentToken}` }
    }, revPayload);
    assert('17. Integrity: Block Self-Review (400)', selfRevRes.status === 400, `status=${selfRevRes.status}`);

    // 7. Doubt Forum
    const doubtPayload = JSON.stringify({
      title: 'How does virtual memory paging translate logical to physical addresses?',
      content: 'I understand page tables but want clarity on how the TLB cache interacts during page faults.',
      subjectId: 3,
      tags: ['virtual-memory', 'paging', 'os']
    });
    const doubtRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/doubts', method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${currentStudentToken}` }
    }, doubtPayload);
    assert('18. Doubt: Create Question', doubtRes.status === 201 && doubtRes.data?.data?.id, `status=${doubtRes.status}`);
    const doubtId = doubtRes.data?.data?.id;

    // Fetch Doubt Detail
    const doubtDetailRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/doubts/${doubtId}`, method: 'GET',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    assert('19. Doubt: Fetch Question Details', doubtDetailRes.status === 200 && doubtDetailRes.data?.data?.id === doubtId, `status=${doubtDetailRes.status}`);

    // Answer Question by Peer
    const answerPayload = JSON.stringify({
      content: 'When the CPU generates a virtual address, the MMU checks the TLB first. On a hit, physical frame is immediate. On a miss, page table walk occurs; if invalid bit, page fault trap occurs to OS.'
    });
    const answerRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/doubts/${doubtId}/answers`, method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${reviewerToken}` }
    }, answerPayload);
    assert('20. Doubt: Post Answer to Question', answerRes.status === 201 && answerRes.data?.data?.id, `status=${answerRes.status}`);
    const answerId = answerRes.data?.data?.id;

    // Upvote Answer
    const upvoteRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/answers/${answerId}/vote`, method: 'POST',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    assert('21. Doubt: Upvote Answer', upvoteRes.status === 200, `status=${upvoteRes.status}`);

    // Accept Answer as Solution (by Question Author)
    const acceptRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/doubts/${doubtId}/accept/${answerId}`, method: 'POST',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    assert('22. Doubt: Accept Answer as Accepted Solution', acceptRes.status === 200, `status=${acceptRes.status}`);

    // 8. Collaborative Whiteboard
    const wbPayload = JSON.stringify({
      title: 'OS Group Revision Room',
      subjectId: 3,
      topic: 'Virtual Memory & Scheduling'
    });
    const wbRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/whiteboard/sessions', method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${currentStudentToken}` }
    }, wbPayload);
    assert('23. Whiteboard: Create Session Room', wbRes.status === 201 && wbRes.data?.data?.sessionCode, `status=${wbRes.status}`);
    const sessionCode = wbRes.data?.data?.sessionCode;

    // Fetch Whiteboard Session by Code
    const wbGetRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/whiteboard/sessions/${sessionCode}`, method: 'GET',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    assert('24. Whiteboard: Get Session by Code', wbGetRes.status === 200 && wbGetRes.data?.data?.sessionCode === sessionCode, `status=${wbGetRes.status}`);

    // Save Whiteboard Snapshot
    const snapPayload = JSON.stringify({
      stateJson: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=='
    });
    const snapRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/whiteboard/sessions/${sessionCode}/snapshots`, method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${currentStudentToken}` }
    }, snapPayload);
    assert('25. Whiteboard: Save Canvas Snapshot', snapRes.status === 201, `status=${snapRes.status}`);

    // 9. Focus Pomodoro Sessions
    const focusPayload = JSON.stringify({
      subjectId: 3,
      mode: 'POMODORO',
      plannedMinutes: 25
    });
    const focusStartRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/focus/sessions/start', method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${currentStudentToken}` }
    }, focusPayload);
    assert('26. Focus: Start Pomodoro Session', focusStartRes.status === 201 && focusStartRes.data?.data?.id, `status=${focusStartRes.status}`);
    const focusSessionId = focusStartRes.data?.data?.id;

    // Conclude Focus Session
    const focusEndRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/focus/sessions/${focusSessionId}/end`, method: 'POST',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    assert('27. Focus: Conclude Pomodoro Session', focusEndRes.status === 200, `status=${focusEndRes.status}`);

    // Focus Stats
    const focusStatsRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/focus/stats', method: 'GET',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    assert('28. Focus: Get Personal Focus Stats', focusStatsRes.status === 200, `status=${focusStatsRes.status}`);

    // Focus Leaderboard
    const focusLbRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/focus/leaderboard', method: 'GET',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    assert('29. Focus: Get Department Leaderboard', focusLbRes.status === 200, `status=${focusLbRes.status}`);

    // 10. AI Subsystem
    const aiAskPayload = JSON.stringify({
      question: 'What is the difference between preemptive and non-preemptive scheduling in operating systems?',
      subjectContext: 'Operating Systems'
    });
    const aiAskRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/ai/doubt-assistant', method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${currentStudentToken}` }
    }, aiAskPayload);
    assert('30. AI: Doubt Assistant Generates Explanation', aiAskRes.status === 200 && !!aiAskRes.data?.data?.directAnswer, `status=${aiAskRes.status}`);

    // AI Resource Summary
    const aiSumRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/ai/summarize/${resourceId}`, method: 'POST',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    const hasSummaryText = !!(aiSumRes.data?.data?.shortSummary || aiSumRes.data?.data?.summary);
    assert('31. AI: Resource Summarizer Generates Key Points', aiSumRes.status === 200 && hasSummaryText, `status=${aiSumRes.status}`);

    // 11. Notifications
    const notifRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/notifications', method: 'GET',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    assert('32. Notifications: Fetch User Notification Feed', notifRes.status === 200, `status=${notifRes.status}`);

    const unreadRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/notifications/unread-count', method: 'GET',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    assert('33. Notifications: Unread Counter Endpoint', unreadRes.status === 200, `status=${unreadRes.status}`);

    // 12. Admin Subsystem
    const adminStatsRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/admin/stats', method: 'GET',
      headers: { 'Authorization': `Bearer ${adminToken}` }
    });
    assert('34. Admin: Platform Analytics Metrics (/admin/stats)', adminStatsRes.status === 200 && adminStatsRes.data?.data?.totalUsers > 0, `status=${adminStatsRes.status}`);

    const adminPendingRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/admin/resources/pending', method: 'GET',
      headers: { 'Authorization': `Bearer ${adminToken}` }
    });
    assert('35. Admin: Pending Resources Queue', adminPendingRes.status === 200, `status=${adminPendingRes.status}`);

    // Admin Moderation Execution
    const modPayload = JSON.stringify({ status: 'APPROVED', rejectionReason: null });
    const modRes = await request({
      hostname: 'localhost', port: 8080, path: `/api/v1/admin/resources/${resourceId}/moderation`, method: 'PATCH',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${adminToken}` }
    }, modPayload);
    assert('36. Admin: Resource Moderation Approval', modRes.status === 200, `status=${modRes.status}`);

    // Admin Users List
    const adminUsersRes = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/admin/users', method: 'GET',
      headers: { 'Authorization': `Bearer ${adminToken}` }
    });
    assert('37. Admin: Users Management List', adminUsersRes.status === 200 && adminUsersRes.data?.data?.totalElements > 0, `status=${adminUsersRes.status}`);

    // 13. Security Boundaries & RBAC
    const studentBlockedFromAdmin = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/admin/stats', method: 'GET',
      headers: { 'Authorization': `Bearer ${currentStudentToken}` }
    });
    assert('38. Security: Student Strictly Forbidden from Admin Stats (403)', studentBlockedFromAdmin.status === 403, `status=${studentBlockedFromAdmin.status}`);

    const anonBlockedFromMe = await request({
      hostname: 'localhost', port: 8080, path: '/api/v1/users/me', method: 'GET'
    });
    assert('39. Security: Unauthenticated Request Denied (401)', anonBlockedFromMe.status === 401, `status=${anonBlockedFromMe.status}`);

    console.log('\n====================================================');
    const passedCount = tests.filter(t => t.passed).length;
    console.log(` AUDIT SUMMARY: ${passedCount} / ${tests.length} TESTS PASSED`);
    console.log('====================================================\n');

    if (passedCount === tests.length) {
      console.log('>>> COMPLETE SYSTEM IS FULLY HEALTHY & OPERATIONAL <<<');
      process.exit(0);
    } else {
      console.log('>>> ATTENTION: SOME AUDIT TESTS FAILED <<<');
      process.exit(1);
    }
  } catch (err) {
    console.error('Audit encountered unexpected execution error:', err);
    process.exit(1);
  }
}

runComprehensiveDebug();
