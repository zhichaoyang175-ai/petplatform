package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.request.PetCreateRequest;
import com.shanhai.petplatform.common.dto.request.PetSearchRequest;
import com.shanhai.petplatform.common.dto.request.PetUpdateRequest;
import com.shanhai.petplatform.common.dto.response.PetDetailVO;
import com.shanhai.petplatform.common.dto.response.PetImageVO;
import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 宠物服务接口
 *
 * @author PetPlatform Team
 */
public interface PetService {

    /** 多条件分页搜索 */
    PageResult<PetVO> searchPets(PetSearchRequest request);

    /** 获取宠物详情 */
    PetDetailVO getPetDetail(Long petId, Long currentUserId);

    /** 发布宠物 */
    PetVO createPet(PetCreateRequest request, Long ownerId);

    /** 更新宠物 */
    PetVO updatePet(Long petId, PetUpdateRequest request, Long ownerId);

    /** 删除宠物（软删除/下架） */
    void deletePet(Long petId, Long ownerId);

    /** 修改宠物状态 */
    PetVO updatePetStatus(Long petId, Integer status, Long ownerId);

    /** 我的宠物列表 */
    PageResult<PetVO> getMyPets(Long ownerId, Integer status, int page, int size);

    /** 上传宠物图片 */
    List<PetImageVO> uploadPetImages(Long petId, MultipartFile[] files, Long userId);

    /** 删除宠物图片 */
    void deletePetImage(Long petId, Long imageId, Long userId);

}
