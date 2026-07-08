package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.dto.request.EvaluationRequest;
import com.shanhai.petplatform.common.dto.response.CreditVO;
import com.shanhai.petplatform.common.dto.response.EvaluationVO;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评价/信用模块 — 提交评价、查询目标评价、查询信用分
 *
 * @author PetPlatform Team
 */
@RestController
@RequestMapping("/api/v1/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    /** 提交评价（@CurrentUser，reviewerId=当前用户） */
    @PostMapping
    public R<EvaluationVO> create(@Valid @RequestBody EvaluationRequest request,
                                  @CurrentUser Long userId) {
        return R.ok(evaluationService.createEvaluation(request, userId));
    }

    /** 按目标查询评价列表（分页） */
    @GetMapping
    public R<PageResult<EvaluationVO>> list(@RequestParam Integer targetType,
                                            @RequestParam Long targetId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return R.ok(evaluationService.getEvaluations(targetType, targetId, page, size));
    }

    /** 查询某目标信用分（平均分 + 评价数） */
    @GetMapping("/credit")
    public R<CreditVO> credit(@RequestParam Integer targetType,
                              @RequestParam Long targetId) {
        return R.ok(evaluationService.getCredit(targetType, targetId));
    }

    /** 查询当前用户作为目标的信用分（targetType=1） */
    @GetMapping("/my-credit")
    public R<CreditVO> myCredit(@CurrentUser Long userId) {
        return R.ok(evaluationService.getMyCredit(userId));
    }

}
