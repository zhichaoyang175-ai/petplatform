package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.request.EvaluationRequest;
import com.shanhai.petplatform.common.dto.response.CreditVO;
import com.shanhai.petplatform.common.dto.response.EvaluationVO;
import com.shanhai.petplatform.common.result.PageResult;

/**
 * 评价/信用服务接口
 *
 * @author PetPlatform Team
 */
public interface EvaluationService {

    /** 提交评价 */
    EvaluationVO createEvaluation(EvaluationRequest request, Long reviewerId);

    /** 按目标分页查询评价列表 */
    PageResult<EvaluationVO> getEvaluations(Integer targetType, Long targetId, int page, int size);

    /** 查询某目标信用分（平均分 + 评价数） */
    CreditVO getCredit(Integer targetType, Long targetId);

    /** 查询当前用户作为目标的信用分（targetType=1） */
    CreditVO getMyCredit(Long userId);

}
