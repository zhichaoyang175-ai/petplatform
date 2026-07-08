package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.request.AdopterApplicationRequest;
import com.shanhai.petplatform.common.dto.response.ReviewTaskVO;
import com.shanhai.petplatform.common.result.PageResult;

/**
 * 通用审核服务
 *
 * @author PetPlatform Team
 */
public interface ReviewService {

    /** 提交送养人认证申请（进入审核流程，不直接改角色） */
    ReviewTaskVO submitAdopterApplication(Long applicantId, AdopterApplicationRequest req);

    /** 查询当前用户最新的送养人认证申请 */
    ReviewTaskVO getMyAdopterApplication(Long applicantId);

    /** 审核端：分页列出审核任务（默认待审核） */
    PageResult<ReviewTaskVO> listPending(Integer status, int page, int size);

    /** 审核端：任务详情 */
    ReviewTaskVO getDetail(Long id);

    /** 审核端：通过 */
    ReviewTaskVO approve(Long id, Long reviewerId, String comment);

    /** 审核端：驳回 */
    ReviewTaskVO reject(Long id, Long reviewerId, String comment);
}
