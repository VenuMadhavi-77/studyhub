package com.studyhub.platform.modules.admin.service;

import com.studyhub.platform.common.enums.ReportStatus;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.modules.admin.dto.ReportCreateRequest;
import com.studyhub.platform.modules.admin.dto.ReportResponse;
import com.studyhub.platform.modules.admin.entity.Report;
import com.studyhub.platform.modules.admin.repository.ReportRepository;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReportResponse submitReport(ReportCreateRequest request, Long reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reporterId));

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetType(request.targetType());
        report.setTargetId(request.targetId());
        report.setReason(request.reason().trim());
        report.setDetails(request.details());
        report.setStatus(ReportStatus.PENDING);

        Report saved = reportRepository.save(report);
        return ReportResponse.fromEntity(saved);
    }
}
