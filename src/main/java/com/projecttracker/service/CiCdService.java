package com.projecttracker.service;

import com.projecttracker.entity.BuildReport;
import java.util.List;
import java.util.Map;

public interface CiCdService {

    /** Đón tiếp webhook và kích hoạt chạy tiến trình test tự động async */
    void processWebhook(Map<String, Object> payload);

    /** Lấy tất cả lịch sử báo cáo kiểm thử */
    List<BuildReport> getBuildReports();
}
