package com.shanhai.petplatform.common.dto.request;

import lombok.Data;

/**
 * 审核操作入参（通过/驳回时附带意见）
 *
 * @author PetPlatform Team
 */
@Data
public class ReviewActionRequest {

    /** 审核意见 / 驳回原因 */
    private String comment;
}
