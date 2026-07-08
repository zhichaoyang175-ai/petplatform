package com.shanhai.petplatform.common.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 管理后台 KPI 统计数据
 *
 * @author PetPlatform Team
 */
@Data
public class AdminStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户总数 */
    private long userTotal;

    /** 送养人数量（role = 2） */
    private long adopterCount;

    /** 宠物总数 */
    private long petTotal;

    /** 待领养宠物数（status = 0） */
    private long pendingPetCount;

    /** 领养申请总数 */
    private long applicationTotal;

    /** 待审核申请数（status = 0） */
    private long pendingApplicationCount;

    /** 成功领养数 */
    private long adoptedCount;

    /** 合作救助站数（无 t_shelter 表时返回 0） */
    private long shelterCount;

    /** 最近审核申请（仪表盘「申请审核」列表，点击进入 /admin/review/:id） */
    private List<RecentReview> recentReviews;

    /** 仪表盘最近审核条目 */
    @Data
    public static class RecentReview implements Serializable {
        private Long id;
        private String applicantName;
        private String petName;
        private String statusName;
        private String statusType;
    }
}
