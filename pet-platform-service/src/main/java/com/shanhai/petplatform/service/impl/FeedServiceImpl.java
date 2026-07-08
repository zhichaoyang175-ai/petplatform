package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanhai.petplatform.common.dto.request.FeedRequest;
import com.shanhai.petplatform.common.dto.response.FeedVO;
import com.shanhai.petplatform.common.exception.NotFoundException;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.repository.entity.Feed;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.mapper.FeedMapper;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import com.shanhai.petplatform.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 领养动态服务实现
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedMapper feedMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_LIKE_COUNT = 0;

    // ────────────────── 公开列表 ──────────────────

    @Override
    public PageResult<FeedVO> listFeeds(int page, int size) {
        LambdaQueryWrapper<Feed> wrapper = new LambdaQueryWrapper<Feed>()
                .orderByDesc(Feed::getCreatedAt);

        Page<Feed> feedPage = feedMapper.selectPage(new Page<>(page, size), wrapper);
        List<FeedVO> voList = feedPage.getRecords().stream()
                .map(this::toVO)
                .toList();

        return PageResult.of(voList, feedPage.getTotal(), (int) feedPage.getCurrent(), (int) feedPage.getSize());
    }

    // ────────────────── 详情 ──────────────────

    @Override
    public FeedVO getFeed(Long feedId) {
        Feed feed = feedMapper.selectById(feedId);
        if (feed == null) {
            throw new NotFoundException("动态不存在");
        }
        return toVO(feed);
    }

    // ────────────────── 发布 ──────────────────

    @Override
    @Transactional
    public FeedVO createFeed(FeedRequest request, Long authorId) {
        User author = userMapper.selectById(authorId);
        if (author == null) {
            throw new NotFoundException("用户不存在");
        }

        Feed feed = new Feed();
        feed.setAuthorId(authorId);
        feed.setAuthorName(author.getNickname());
        feed.setPetId(request.getPetId());
        feed.setContent(request.getContent());
        feed.setImages(serializeImages(request.getImages()));
        feed.setLikeCount(DEFAULT_LIKE_COUNT);

        feedMapper.insert(feed);
        log.info("发布动态: feedId={}, authorId={}", feed.getId(), authorId);

        return toVO(feed);
    }

    // ────────────────── 点赞 ──────────────────

    @Override
    @Transactional
    public void likeFeed(Long feedId) {
        Feed feed = feedMapper.selectById(feedId);
        if (feed == null) {
            throw new NotFoundException("动态不存在");
        }
        feed.setLikeCount((feed.getLikeCount() == null ? 0 : feed.getLikeCount()) + 1);
        feedMapper.updateById(feed);
    }

    // ────────────────── 我的动态 ──────────────────

    @Override
    public PageResult<FeedVO> getMyFeeds(Long authorId, int page, int size) {
        LambdaQueryWrapper<Feed> wrapper = new LambdaQueryWrapper<Feed>()
                .eq(Feed::getAuthorId, authorId)
                .orderByDesc(Feed::getCreatedAt);

        Page<Feed> feedPage = feedMapper.selectPage(new Page<>(page, size), wrapper);
        List<FeedVO> voList = feedPage.getRecords().stream()
                .map(this::toVO)
                .toList();

        return PageResult.of(voList, feedPage.getTotal(), (int) feedPage.getCurrent(), (int) feedPage.getSize());
    }

    // ────────────────── 工具 ──────────────────

    /** 实体转 VO（含 images JSON 解析） */
    private FeedVO toVO(Feed feed) {
        return FeedVO.of(
                feed.getId(),
                feed.getAuthorId(),
                feed.getAuthorName(),
                feed.getPetId(),
                feed.getContent(),
                parseImages(feed.getImages()),
                feed.getLikeCount(),
                feed.getCreatedAt()
        );
    }

    /** 将图片 URL 列表序列化为 JSON 数组字符串 */
    private String serializeImages(List<String> images) {
        if (images == null || images.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            log.warn("动态图片序列化失败: {}", e.getMessage());
            return null;
        }
    }

    /** 将 JSON 数组字符串解析回图片 URL 列表 */
    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) return List.of();
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("动态图片解析失败: {}", e.getMessage());
            return List.of();
        }
    }

}
