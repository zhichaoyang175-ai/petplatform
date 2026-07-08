package com.shanhai.petplatform.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shanhai.petplatform.common.enums.GenderEnum;
import com.shanhai.petplatform.common.enums.HealthStatusEnum;
import com.shanhai.petplatform.common.enums.PetStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物信息 VO — 列表页和搜索结果使用
 *
 * @author PetPlatform Team
 */
@Data
public class PetVO {

    /** 宠物ID */
    private Long id;

    /** 宠物名称 */
    private String name;

    /** 品种 */
    private String breed;

    /** 性别 code */
    private Integer gender;

    /** 性别名称 */
    private String genderName;

    /** 月龄 */
    private Integer ageMonths;

    /** 体重(kg) */
    private BigDecimal weightKg;

    /** 是否绝育 */
    private Boolean neutered;

    /** 健康状态 code */
    private Integer healthStatus;

    /** 健康状态名称 */
    private String healthStatusName;

    /** 所在省 */
    private String locationProvince;

    /** 所在市 */
    private String locationCity;

    /** 完整地区（省-市） */
    private String fullLocation;

    /** 详细描述 */
    private String description;

    /** 状态 code */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 浏览次数 */
    private Integer viewCount;

    /** 封面图片 URL */
    private String coverImage;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 构建列表 VO */
    public static PetVO of(Long id, String name, String breed,
                           Integer gender, Integer ageMonths, BigDecimal weightKg,
                           Integer neutered, Integer healthStatus,
                           String locationProvince, String locationCity,
                           String description, Integer status, Integer viewCount,
                           String coverImage, LocalDateTime createdAt) {
        PetVO vo = new PetVO();
        vo.setId(id);
        vo.setName(name);
        vo.setBreed(breed);
        vo.setGender(gender);
        vo.setGenderName(GenderEnum.fromCode(gender).getDesc());
        vo.setAgeMonths(ageMonths);
        vo.setWeightKg(weightKg);
        vo.setNeutered(neutered != null && neutered == 1);
        vo.setHealthStatus(healthStatus);
        vo.setHealthStatusName(HealthStatusEnum.fromCode(healthStatus).getDesc());
        vo.setLocationProvince(locationProvince);
        vo.setLocationCity(locationCity);
        vo.setFullLocation(buildFullLocation(locationProvince, locationCity));
        vo.setDescription(description);
        vo.setStatus(status);
        vo.setStatusName(PetStatusEnum.fromCode(status).getDesc());
        vo.setViewCount(viewCount);
        vo.setCoverImage(coverImage);
        vo.setCreatedAt(createdAt);
        return vo;
    }

    private static String buildFullLocation(String province, String city) {
        if (province == null && city == null) return null;
        if (province == null) return city;
        if (city == null) return province;
        return province + " · " + city;
    }

}
