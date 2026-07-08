package com.shanhai.petplatform.common.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 管理后台仪表盘 VO
 *
 * @author PetPlatform Team
 */
@Data
public class DashboardVO {

    /** 待审核申请数 */
    private Long pendingReviews;

    /** 已审核通过数 */
    private Long approved;

    /** 待回访数 */
    private Long pendingFollowUp;

    /** 本月领养数 */
    private Long monthlyAdoptions;

    /** 最近审核记录 */
    private List<ApplicationVO> recentReviews;

    /** 构建 VO */
    public static DashboardVO of(Long pendingReviews, Long approved,
                                  Long pendingFollowUp, Long monthlyAdoptions,
                                  List<ApplicationVO> recentReviews) {
        DashboardVO vo = new DashboardVO();
        vo.setPendingReviews(pendingReviews);
        vo.setApproved(approved);
        vo.setPendingFollowUp(pendingFollowUp);
        vo.setMonthlyAdoptions(monthlyAdoptions);
        vo.setRecentReviews(recentReviews);
        return vo;
    }

}
