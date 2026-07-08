package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.request.ShelterRequest;
import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.dto.response.ShelterVO;
import com.shanhai.petplatform.common.result.PageResult;

import java.util.List;

/**
 * 救助站服务接口
 *
 * @author PetPlatform Team
 */
public interface ShelterService {

    /** 分页公开列表（支持 province/city/keyword 过滤） */
    PageResult<ShelterVO> listShelters(String province, String city, String keyword, int page, int size);

    /** 公开详情 */
    ShelterVO getShelter(Long shelterId);

    /** 该救助站在养宠物列表（按 owner_user_id 关联 t_pet） */
    List<PetVO> getShelterPets(Long shelterId);

    /** 创建救助站（当前登录用户为创建者，状态默认待审核） */
    ShelterVO createShelter(ShelterRequest request, Long userId);

    /** 更新救助站（仅创建者或管理员） */
    ShelterVO updateShelter(Long shelterId, ShelterRequest request, Long userId);

    /** 我的救助站 */
    List<ShelterVO> getMyShelters(Long userId);

}
