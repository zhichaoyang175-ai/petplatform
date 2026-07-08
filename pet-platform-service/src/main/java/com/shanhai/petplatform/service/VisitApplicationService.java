package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.request.VisitBookRequest;
import com.shanhai.petplatform.common.dto.response.VisitVO;

import java.util.List;

/**
 * 预约看宠服务接口
 */
public interface VisitApplicationService {

    /** 发起预约（申请人视角） */
    VisitVO book(VisitBookRequest request, Long applicantId);

    /** 我发起的预约（申请人视角） */
    List<VisitVO> myVisits(Long applicantId);

    /** 我收到的预约（送养人视角） */
    List<VisitVO> receivedVisits(Long ownerId);

    /** 送养人确认预约 */
    VisitVO confirm(Long id, Long ownerId);

    /** 完成预约（申请人或送养人均可） */
    VisitVO complete(Long id, Long userId);

    /** 取消预约（申请人或送养人均可） */
    VisitVO cancel(Long id, Long userId);

}
