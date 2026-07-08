package com.shanhai.petplatform.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 领养动态 VO
 *
 * @author PetPlatform Team
 */
@Data
public class FeedVO {

    /** 动态ID */
    private Long id;

    /** 作者用户ID */
    private Long authorId;

    /** 作者昵称 */
    private String authorName;

    /** 关联宠物ID */
    private Long petId;

    /** 动态内容 */
    private String content;

    /** 图片 URL 列表 */
    private List<String> images;

    /** 点赞数 */
    private Integer likeCount;

    /** 创建时间（格式化） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 构建 VO */
    public static FeedVO of(Long id, Long authorId, String authorName, Long petId,
                            String content, List<String> images, Integer likeCount,
                            LocalDateTime createdAt) {
        FeedVO vo = new FeedVO();
        vo.setId(id);
        vo.setAuthorId(authorId);
        vo.setAuthorName(authorName);
        vo.setPetId(petId);
        vo.setContent(content);
        vo.setImages(images);
        vo.setLikeCount(likeCount);
        vo.setCreatedAt(createdAt);
        return vo;
    }

}
