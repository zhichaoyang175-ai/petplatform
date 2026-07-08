package com.shanhai.petplatform.api.controller;

import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.result.PageResult;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.infrastructure.security.CurrentUser;
import com.shanhai.petplatform.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public R<Void> add(@RequestBody Map<String, Long> body, @CurrentUser Long userId) {
        favoriteService.addFavorite(body.get("petId"), userId);
        return R.ok();
    }

    @DeleteMapping("/{petId}")
    public R<Void> remove(@PathVariable Long petId, @CurrentUser Long userId) {
        favoriteService.removeFavorite(petId, userId);
        return R.ok();
    }

    @GetMapping
    public R<PageResult<PetVO>> list(@CurrentUser Long userId,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return R.ok(favoriteService.getFavorites(userId, page, size));
    }

    @GetMapping("/check/{petId}")
    public R<Map<String, Boolean>> check(@PathVariable Long petId, @CurrentUser Long userId) {
        return R.ok(Map.of("favorited", favoriteService.isFavorited(petId, userId)));
    }
}
