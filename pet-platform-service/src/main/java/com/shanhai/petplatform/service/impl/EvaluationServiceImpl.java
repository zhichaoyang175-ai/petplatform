package com.shanhai.petplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanhai.petplatform.common.dto.request.EvaluationRequest;
import com.shanhai.petplatform.common.dto.response.CreditVO;
import com.shanhai.petplatform.common.dto.response.EvaluationVO;
import com.shanhai.petplatform.common.enums.EvaluationTargetTypeEnum;
import com.shanhai.petplatform.common.exception.BusinessException;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.repository.entity.Evaluation;
import com.shanhai.petplatform.repository.entity.User;
import com.shanhai.petplatform.repository.mapper.EvaluationMapper;
import com.shanhai.petplatform.repository.mapper.UserMapper;
import com.shanhai.petplatform.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 评价/信用服务实现
 *
 * @author PetPlatform Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationMapper evaluationMapper;
    private final UserMapper userMapper;

    private static final int TARGET_TYPE_USER = 1;

    // ────────────────── 提交评价 ──────────────────

    @Override
    public EvaluationVO createEvaluation(EvaluationRequest request, Long reviewerId) {
        if (request.getTargetType() == null || !EvaluationTargetTypeEnum.isValid(request.getTargetType())) {
            throw new BusinessException("评价目标类型不合法（1-用户 2-救助站）");
        }
        if (request.getTargetId() == null) {
            throw new BusinessException("评价目标ID不能为空");
        }
        if (request.getScore() == null || request.getScore() < 1 || request.getScore() > 5) {
            throw new BusinessException("评分必须在 1-5 之间");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setReviewerId(reviewerId);
        evaluation.setTargetType(request.getTargetType());
        evaluation.setTargetId(request.getTargetId());
        evaluation.setScore(request.getScore());
        evaluation.setComment(request.getComment());

        evaluationMapper.insert(evaluation);
        log.info("提交评价: evaluationId={}, reviewerId={}, targetType={}, targetId={}",
                evaluation.getId(), reviewerId, request.getTargetType(), request.getTargetId());

        return toVO(evaluation);
    }

    // ────────────────── 评价列表 ──────────────────

    @Override
    public PageResult<EvaluationVO> getEvaluations(Integer targetType, Long targetId, int page, int size) {
        if (targetType == null || targetId == null) {
            throw new BusinessException("评价目标类型和ID不能为空");
        }
        Page<Evaluation> p = new Page<>(page, size);
        Page<Evaluation> evaluationPage = evaluationMapper.selectPage(p, new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getTargetType, targetType)
                .eq(Evaluation::getTargetId, targetId)
                .orderByDesc(Evaluation::getCreatedAt));

        List<EvaluationVO> voList = evaluationPage.getRecords().stream()
                .map(this::toVO)
                .toList();

        return PageResult.of(voList, evaluationPage.getTotal(),
                (int) evaluationPage.getCurrent(), (int) evaluationPage.getSize());
    }

    // ────────────────── 信用分 ──────────────────

    @Override
    public CreditVO getCredit(Integer targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            throw new BusinessException("评价目标类型和ID不能为空");
        }
        return computeCredit(targetType, targetId);
    }

    @Override
    public CreditVO getMyCredit(Long userId) {
        return computeCredit(TARGET_TYPE_USER, userId);
    }

    /** 计算平均分与数量 */
    private CreditVO computeCredit(Integer targetType, Long targetId) {
        List<Evaluation> list = evaluationMapper.selectList(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getTargetType, targetType)
                .eq(Evaluation::getTargetId, targetId)
                .select(Evaluation::getScore));

        long count = list.size();
        BigDecimal avgScore = BigDecimal.ZERO;
        if (count > 0) {
            double sum = list.stream().mapToInt(Evaluation::getScore).sum();
            avgScore = BigDecimal.valueOf(sum / (double) count)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return CreditVO.of(targetType, targetId, avgScore, count);
    }

    // ────────────────── 工具 ──────────────────

    /** 实体转 VO（补全评价人昵称） */
    private EvaluationVO toVO(Evaluation evaluation) {
        String reviewerName = null;
        if (evaluation.getReviewerId() != null) {
            User reviewer = userMapper.selectById(evaluation.getReviewerId());
            if (reviewer != null) {
                reviewerName = reviewer.getNickname() != null ? reviewer.getNickname() : reviewer.getPhone();
            }
        }
        return EvaluationVO.of(
                evaluation.getId(),
                evaluation.getReviewerId(),
                reviewerName,
                evaluation.getTargetType(),
                evaluation.getTargetId(),
                evaluation.getScore(),
                evaluation.getComment(),
                evaluation.getCreatedAt());
    }

}
