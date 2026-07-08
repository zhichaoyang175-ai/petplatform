package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shanhai.petplatform.common.dto.request.AdopterApplicationRequest;
import com.shanhai.petplatform.common.dto.response.ReviewTaskVO;
import com.shanhai.petplatform.common.enums.NotificationTypeEnum;
import com.shanhai.petplatform.common.enums.ReviewStatusEnum;
import com.shanhai.petplatform.common.enums.ReviewTypeEnum;
import com.shanhai.petplatform.common.enums.UserRoleEnum;
import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.repository.entity.ReviewTask;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.mapper.ReviewTaskMapper;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import com.shanhai.petplatform.service.NotificationService;
import com.shanhai.petplatform.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通用审核服务实现
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewTaskMapper reviewTaskMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将审核任务实体转换为 VO（转换放在 service 层，避免 common 模块反向依赖 repository 实体）。
     */
    private ReviewTaskVO toVO(ReviewTask t, String applicantName) {
        ReviewTaskVO vo = new ReviewTaskVO();
        vo.setId(t.getId());
        vo.setApplicantId(t.getApplicantId());
        vo.setApplicantName(applicantName);
        vo.setType(t.getType());
        vo.setTypeName(ReviewTypeEnum.fromCode(t.getType()).getDesc());
        vo.setStatus(t.getStatus());
        vo.setStatusName(ReviewStatusEnum.fromCode(t.getStatus()).getDesc());
        vo.setTitle(t.getTitle());
        vo.setSubmitData(t.getSubmitData());
        vo.setReviewerId(t.getReviewerId());
        vo.setReviewComment(t.getReviewComment());
        vo.setReviewedAt(t.getReviewedAt());
        vo.setCreatedAt(t.getCreatedAt());
        return vo;
    }


    @Override
    @Transactional
    public ReviewTaskVO submitAdopterApplication(Long applicantId, AdopterApplicationRequest req) {
        User user = userMapper.selectById(applicantId);
        if (user == null) throw new BusinessException("用户不存在");
        if (user.getRole() != null && user.getRole() == UserRoleEnum.ADOPTER.getCode())
            throw new BusinessException("您已是送养人，无需重复申请");

        // 避免重复提交：已有待审核申请则直接返回
        ReviewTask existing = reviewTaskMapper.selectOne(new LambdaQueryWrapper<ReviewTask>()
                .eq(ReviewTask::getApplicantId, applicantId)
                .eq(ReviewTask::getType, ReviewTypeEnum.ADOPTER_APPLY.getCode())
                .eq(ReviewTask::getStatus, ReviewStatusEnum.PENDING.getCode())
                .orderByDesc(ReviewTask::getCreatedAt).last("LIMIT 1"));
        if (existing != null) return toVO(existing, user.getNickname());

        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("realName", req.getRealName());
            data.put("idCard", req.getIdCard());
            data.put("phone", req.getPhone());
            data.put("email", req.getEmail());
            data.put("reason", req.getReason());
            String json = objectMapper.writeValueAsString(data);

            ReviewTask task = new ReviewTask();
            task.setApplicantId(applicantId);
            task.setType(ReviewTypeEnum.ADOPTER_APPLY.getCode());
            task.setStatus(ReviewStatusEnum.PENDING.getCode());
            task.setTitle("送养人认证申请 - " + (req.getRealName() != null ? req.getRealName() : user.getNickname()));
            task.setSubmitData(json);
            reviewTaskMapper.insert(task);

            notificationService.notify(applicantId, "送养人认证已提交",
                    "您的送养人认证申请已提交，我们将尽快审核。", NotificationTypeEnum.REVIEW.getCode(),
                    "review", task.getId());
            return toVO(task, user.getNickname());
        } catch (Exception e) {
            throw new BusinessException("提交失败：" + e.getMessage());
        }
    }

    @Override
    public ReviewTaskVO getMyAdopterApplication(Long applicantId) {
        ReviewTask task = reviewTaskMapper.selectOne(new LambdaQueryWrapper<ReviewTask>()
                .eq(ReviewTask::getApplicantId, applicantId)
                .eq(ReviewTask::getType, ReviewTypeEnum.ADOPTER_APPLY.getCode())
                .orderByDesc(ReviewTask::getCreatedAt).last("LIMIT 1"));
        if (task == null) return null;
        User user = userMapper.selectById(applicantId);
        return toVO(task, user != null ? user.getNickname() : "");
    }

    @Override
    public PageResult<ReviewTaskVO> listPending(Integer status, int page, int size) {
        Integer st = status == null ? ReviewStatusEnum.PENDING.getCode() : status;
        LambdaQueryWrapper<ReviewTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewTask::getStatus, st).orderByDesc(ReviewTask::getCreatedAt);
        Page<ReviewTask> p = new Page<>(page, size);
        Page<ReviewTask> result = reviewTaskMapper.selectPage(p, wrapper);
        List<ReviewTaskVO> list = result.getRecords().stream().map(t -> {
            User u = userMapper.selectById(t.getApplicantId());
            return toVO(t, u != null ? u.getNickname() : "");
        }).collect(Collectors.toList());
        return PageResult.of(list, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public ReviewTaskVO getDetail(Long id) {
        ReviewTask task = reviewTaskMapper.selectById(id);
        if (task == null) throw new BusinessException("审核任务不存在");
        User u = userMapper.selectById(task.getApplicantId());
        return toVO(task, u != null ? u.getNickname() : "");
    }

    @Override
    @Transactional
    public ReviewTaskVO approve(Long id, Long reviewerId, String comment) {
        ReviewTask task = reviewTaskMapper.selectById(id);
        if (task == null) throw new BusinessException("审核任务不存在");
        if (ReviewStatusEnum.PENDING.getCode() != task.getStatus())
            throw new BusinessException("该申请已处理，无法重复审核");

        task.setStatus(ReviewStatusEnum.APPROVED.getCode());
        task.setReviewerId(reviewerId);
        task.setReviewComment(comment);
        task.setReviewedAt(LocalDateTime.now());
        reviewTaskMapper.updateById(task);

        if (ReviewTypeEnum.ADOPTER_APPLY.getCode() == task.getType()) {
            User user = userMapper.selectById(task.getApplicantId());
            if (user != null) {
                user.setRole(UserRoleEnum.ADOPTER.getCode());
                userMapper.updateById(user);
            }
        }

        notificationService.notify(task.getApplicantId(), "送养人认证已通过",
                "恭喜，您的送养人认证申请已通过，现在可以发布领养信息了。",
                NotificationTypeEnum.REVIEW_RESULT.getCode(), "review", task.getId());
        User u = userMapper.selectById(task.getApplicantId());
        return toVO(task, u != null ? u.getNickname() : "");
    }

    @Override
    @Transactional
    public ReviewTaskVO reject(Long id, Long reviewerId, String comment) {
        ReviewTask task = reviewTaskMapper.selectById(id);
        if (task == null) throw new BusinessException("审核任务不存在");
        if (ReviewStatusEnum.PENDING.getCode() != task.getStatus())
            throw new BusinessException("该申请已处理，无法重复审核");

        task.setStatus(ReviewStatusEnum.REJECTED.getCode());
        task.setReviewerId(reviewerId);
        task.setReviewComment(comment);
        task.setReviewedAt(LocalDateTime.now());
        reviewTaskMapper.updateById(task);

        notificationService.notify(task.getApplicantId(), "送养人认证未通过",
                "很抱歉，您的送养人认证申请未通过。原因：" + (comment != null ? comment : "资料不符合要求"),
                NotificationTypeEnum.REVIEW_RESULT.getCode(), "review", task.getId());
        User u = userMapper.selectById(task.getApplicantId());
        return toVO(task, u != null ? u.getNickname() : "");
    }
}
