package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.response.AdminStatsVO;

/**
 * 管理后台 KPI 统计服务
 *
 * @author PetPlatform Team
 */
public interface AdminStatsService {

    /**
     * 获取管理后台仪表盘 KPI 统计
     *
     * @return 平台各项统计指标
     */
    AdminStatsVO getDashboardStats();
}
