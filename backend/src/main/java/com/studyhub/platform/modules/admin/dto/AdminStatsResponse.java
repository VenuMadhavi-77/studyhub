package com.studyhub.platform.modules.admin.dto;

public record AdminStatsResponse(
        long totalUsers,
        long totalResources,
        long pendingResources,
        long totalDoubts,
        long totalReports,
        long pendingReports,
        long totalDownloads,
        long totalViews
) {}
