package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.response.AdoptionRecordVO;
import com.shanhai.petplatform.common.result.PageResult;

/**
 * 领养记录服务接口
 */
public interface AdoptionRecordService {

    AdoptionRecordVO getAdoptionRecord(Long id);

    PageResult<AdoptionRecordVO> getMyAdoptions(Long userId, Integer status, int page, int size);

    PageResult<AdoptionRecordVO> getMySentPets(Long userId, Integer status, int page, int size);

}
