package com.shanhai.petplatform.common.dto.response;

import lombok.Data;

/**
 * 平台统计数据 VO
 *
 * @author PetPlatform Team
 */
@Data
public class StatsVO {

    /** 待领养宠物数 */
    private Long pendingAdoption;

    /** 成功领养数 */
    private Long successfulAdoption;

    /** 合作救助站数 */
    private Long partnerShelters;

    /** 构建 VO */
    public static StatsVO of(Long pendingAdoption, Long successfulAdoption, Long partnerShelters) {
        StatsVO vo = new StatsVO();
        vo.setPendingAdoption(pendingAdoption);
        vo.setSuccessfulAdoption(successfulAdoption);
        vo.setPartnerShelters(partnerShelters);
        return vo;
    }

}
