package com.shanhai.petplatform.common.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 回访提交请求
 *
 * @author PetPlatform Team
 */
@Data
public class FollowUpSubmitRequest {

    /** 文字描述 */
    private String content;

    /** 回访照片URL列表 */
    private List<String> imageUrls;

}
