package com.shanhai.petplatform.common.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 宠物详情 VO — 继承 PetVO，附加完整信息
 *
 * @author PetPlatform Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PetDetailVO extends PetVO {

    /** 领养要求 */
    private String adoptionRequirements;

    /** 宠物图片列表 */
    private List<PetImageVO> images;

    /** 送养人信息 */
    private UserVO owner;

    /** 当前用户是否已收藏 */
    private Boolean isFavorited;

    /** 相似宠物推荐 */
    private List<PetVO> similarPets;

    /** 从 PetVO 构建详情 */
    public static PetDetailVO fromPetVO(PetVO petVO, String adoptionRequirements,
                                         List<PetImageVO> images, UserVO owner,
                                         Boolean isFavorited, List<PetVO> similarPets) {
        PetDetailVO vo = new PetDetailVO();
        // 复制 PetVO 字段
        vo.setId(petVO.getId());
        vo.setName(petVO.getName());
        vo.setBreed(petVO.getBreed());
        vo.setGender(petVO.getGender());
        vo.setGenderName(petVO.getGenderName());
        vo.setAgeMonths(petVO.getAgeMonths());
        vo.setWeightKg(petVO.getWeightKg());
        vo.setNeutered(petVO.getNeutered());
        vo.setHealthStatus(petVO.getHealthStatus());
        vo.setHealthStatusName(petVO.getHealthStatusName());
        vo.setLocationProvince(petVO.getLocationProvince());
        vo.setLocationCity(petVO.getLocationCity());
        vo.setFullLocation(petVO.getFullLocation());
        vo.setDescription(petVO.getDescription());
        vo.setStatus(petVO.getStatus());
        vo.setStatusName(petVO.getStatusName());
        vo.setViewCount(petVO.getViewCount());
        vo.setCoverImage(petVO.getCoverImage());
        vo.setCreatedAt(petVO.getCreatedAt());
        // PetDetailVO 专属字段
        vo.setAdoptionRequirements(adoptionRequirements);
        vo.setImages(images);
        vo.setOwner(owner);
        vo.setIsFavorited(isFavorited);
        vo.setSimilarPets(similarPets);
        return vo;
    }

}
