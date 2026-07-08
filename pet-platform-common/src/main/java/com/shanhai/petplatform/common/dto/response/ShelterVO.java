package com.shanhai.petplatform.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shanhai.petplatform.common.enums.ShelterStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 救助站 VO
 *
 * @author PetPlatform Team
 */
@Data
public class ShelterVO {

    /** 救助站ID */
    private Long id;

    /** 创建者用户ID */
    private Long ownerUserId;

    /** 名称 */
    private String name;

    /** 简介/描述 */
    private String description;

    /** 详细地址 */
    private String address;

    /** 所在省 */
    private String province;

    /** 所在市 */
    private String city;

    /** 完整地区（省 · 市） */
    private String fullLocation;

    /** 联系手机号 */
    private String contactPhone;

    /** LOGO/头像 URL */
    private String logoUrl;

    /** 状态 code */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /** 构建 VO */
    public static ShelterVO of(Long id, Long ownerUserId, String name, String description,
                               String address, String province, String city,
                               String contactPhone, String logoUrl, Integer status,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        ShelterVO vo = new ShelterVO();
        vo.setId(id);
        vo.setOwnerUserId(ownerUserId);
        vo.setName(name);
        vo.setDescription(description);
        vo.setAddress(address);
        vo.setProvince(province);
        vo.setCity(city);
        vo.setFullLocation(buildFullLocation(province, city));
        vo.setContactPhone(contactPhone);
        vo.setLogoUrl(logoUrl);
        vo.setStatus(status);
        vo.setStatusName(ShelterStatusEnum.fromCode(status).getDesc());
        vo.setCreatedAt(createdAt);
        vo.setUpdatedAt(updatedAt);
        return vo;
    }

    private static String buildFullLocation(String province, String city) {
        if (province == null && city == null) return null;
        if (province == null) return city;
        if (city == null) return province;
        return province + " · " + city;
    }

}
