package com.shanhai.petplatform.service;

import com.shanhai.petplatform.common.dto.request.FollowUpSubmitRequest;
import com.shanhai.petplatform.common.dto.response.FollowUpRecordVO;
import com.shanhai.petplatform.common.dto.response.FollowUpTaskVO;
import com.shanhai.petplatform.common.result.PageResult;

/**
 * 回访服务接口
 */
public interface FollowUpService {

    void generateFollowUpPlan(Long adoptionRecordId, int totalMonths);

    /**
     * 判断领养记录是否已生成回访计划（用于 MQ 消费者幂等）。
     */
    boolean hasFollowUpPlan(Long adoptionRecordId);

    PageResult<FollowUpTaskVO> getMyTasks(Long userId, Integer status, int page, int size);

    FollowUpTaskVO getTaskDetail(Long taskId, Long userId);

    FollowUpRecordVO submitFollowUp(Long taskId, FollowUpSubmitRequest req, Long userId);

    PageResult<FollowUpRecordVO> getRecords(Long adoptionRecordId, int page, int size);

}
