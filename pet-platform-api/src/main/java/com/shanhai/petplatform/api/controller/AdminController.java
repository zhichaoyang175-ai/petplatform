package com.shanhai.petplatform.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanhai.petplatform.common.dto.response.AdminStatsVO;
import com.shanhai.petplatform.common.dto.response.ApplicationVO;
import com.shanhai.petplatform.common.dto.response.DashboardVO;
import com.shanhai.petplatform.common.dto.response.PetVO;
import com.shanhai.petplatform.common.dto.response.UserVO;
import com.shanhai.petplatform.common.result.R;
import com.shanhai.petplatform.repository.entity.*;
import com.shanhai.petplatform.repository.mapper.*;
import com.shanhai.petplatform.service.AdminStatsService;
import com.shanhai.petplatform.service.state.AdoptionStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdoptionApplicationMapper appMapper;
    private final AdoptionRecordMapper recordMapper;
    private final FollowUpTaskMapper taskMapper;
    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final AdminStatsService adminStatsService;
    private final AdoptionStateMachine adoptionStateMachine;

    // ─── Dashboard ───

    @GetMapping("/dashboard")
    public R<DashboardVO> dashboard() {
        long pending = appMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>().in(AdoptionApplication::getStatus, 0, 1));
        long approved = appMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>().eq(AdoptionApplication::getStatus, 2));
        long pendingFU = taskMapper.selectCount(new LambdaQueryWrapper<FollowUpTask>().in(FollowUpTask::getStatus, 0, 1));
        long monthly = recordMapper.selectCount(new LambdaQueryWrapper<AdoptionRecord>()
                .ge(AdoptionRecord::getAdoptedAt, LocalDate.now().withDayOfMonth(1).atStartOfDay()));

        // 最近10条审核
        List<AdoptionApplication> recent = appMapper.selectPage(
                new Page<>(1, 10),
                new LambdaQueryWrapper<AdoptionApplication>().orderByDesc(AdoptionApplication::getCreatedAt)).getRecords();

        List<ApplicationVO> recentVOs = recent.stream().map(a -> {
            Pet p = petMapper.selectById(a.getPetId());
            return ApplicationVO.of(a.getId(), a.getPetId(), p != null ? p.getName() : "未知",
                    p != null ? p.getBreed() : "未知", p != null && p.getBreed() != null && p.getBreed().contains("猫") ? "🐱" : "🐕",
                    a.getApplicantId(), null, a.getHousingType(), a.getPetExperience(),
                    a.getMonthlyIncome(), a.getFamilyAttitude(), a.getCurrentPets(), a.getReason(),
                    a.getStatus(), a.getRejectReason(), a.getCreatedAt());
        }).toList();

        return R.ok(DashboardVO.of(pending, approved, pendingFU, monthly, recentVOs));
    }

    // ─── Stats (KPI) ───

    @GetMapping("/stats")
    public R<AdminStatsVO> stats() {
        return R.ok(adminStatsService.getDashboardStats());
    }

    // ─── 领养申请审核（管理员视角，可越过送养人归属校验） ───

    @GetMapping("/applications/{id}")
    public R<ApplicationVO> adminApplicationDetail(@PathVariable Long id) {
        AdoptionApplication a = appMapper.selectById(id);
        if (a == null) return R.fail(404, "申请不存在");
        Pet p = petMapper.selectById(a.getPetId());
        return R.ok(ApplicationVO.of(a.getId(), a.getPetId(), p != null ? p.getName() : "未知",
                p != null ? p.getBreed() : "未知", p != null && p.getBreed() != null && p.getBreed().contains("猫") ? "🐱" : "🐕",
                a.getApplicantId(), null, a.getHousingType(), a.getPetExperience(),
                a.getMonthlyIncome(), a.getFamilyAttitude(), a.getCurrentPets(), a.getReason(),
                a.getStatus(), a.getRejectReason(), a.getCreatedAt()));
    }

    @PutMapping("/applications/{id}/approve")
    public R<ApplicationVO> adminApprove(@PathVariable Long id) {
        return R.ok(adoptionStateMachine.adminReview(id, "approve", null));
    }

    @PutMapping("/applications/{id}/reject")
    public R<ApplicationVO> adminReject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(adoptionStateMachine.adminReview(id, "reject", body != null ? body.get("rejectReason") : null));
    }

    // ─── Trend ───

    @GetMapping("/trend")
    public R<List<Map<String, Object>>> trend() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<AdoptionRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<AdoptionRecord>().ge(AdoptionRecord::getAdoptedAt, thirtyDaysAgo));

        Map<LocalDate, Long> grouped = new LinkedHashMap<>();
        for (int i = 29; i >= 0; i--) grouped.put(LocalDate.now().minusDays(i), 0L);
        for (AdoptionRecord r : records) grouped.merge(r.getAdoptedAt().toLocalDate(), 1L, Long::sum);

        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((date, count) -> result.add(Map.of("date", date.toString(), "count", count)));
        return R.ok(result);
    }

    // ─── Users ───

    @GetMapping("/users")
    public R<Object> users(@RequestParam(required = false) String phone,
                            @RequestParam(required = false) Integer role,
                            @RequestParam(required = false) Integer status,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (phone != null) w.like(User::getPhone, phone);
        if (role != null) w.eq(User::getRole, role);
        if (status != null) w.eq(User::getStatus, status);
        w.orderByDesc(User::getCreatedAt);

        Page<User> p = userMapper.selectPage(new Page<>(page, size), w);
        List<UserVO> vos = p.getRecords().stream()
                .map(u -> UserVO.of(u.getId(), maskPhone(u.getPhone()), u.getNickname(),
                        u.getAvatarUrl(), u.getEmail(), u.getRole(), u.getCreatedAt())).toList();

        return R.ok(new Paginated<>(vos, p.getTotal(), (int) p.getCurrent(), (int) p.getSize()));
    }

    @PutMapping("/users/{id}/status")
    public R<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User u = userMapper.selectById(id);
        if (u != null) {
            u.setStatus(body.get("status"));
            userMapper.updateById(u);
        }
        return R.ok();
    }

    // ─── Pets ───

    @GetMapping("/pets")
    public R<Object> adminPets(@RequestParam(required = false) Integer status,
                                @RequestParam(required = false) String breed,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<Pet> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(Pet::getStatus, status);
        if (breed != null) w.eq(Pet::getBreed, breed);
        w.orderByDesc(Pet::getCreatedAt);

        Page<Pet> p = petMapper.selectPage(new Page<>(page, size), w);
        List<PetVO> vos = p.getRecords().stream().map(pet -> PetVO.of(pet.getId(), pet.getName(), pet.getBreed(),
                pet.getGender(), pet.getAgeMonths(), pet.getWeightKg(),
                pet.getNeutered(), pet.getHealthStatus(),
                pet.getLocationProvince(), pet.getLocationCity(),
                pet.getDescription(), pet.getStatus(), pet.getViewCount(), null, pet.getCreatedAt())).toList();
        return R.ok(new Paginated<>(vos, p.getTotal(), (int) p.getCurrent(), (int) p.getSize()));
    }

    @PutMapping("/pets/{id}/status")
    public R<Void> updatePetStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Pet pet = petMapper.selectById(id);
        if (pet != null) {
            pet.setStatus(body.get("status"));
            petMapper.updateById(pet);
        }
        return R.ok();
    }

    // ─── Statistics ───

    @GetMapping("/statistics")
    public R<Map<String, Object>> statistics() {
        long totalUsers = userMapper.selectCount(null);
        long totalPets = petMapper.selectCount(null);
        long totalAdoptions = recordMapper.selectCount(null);
        long todayUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreatedAt, LocalDate.now().atStartOfDay()));
        long todayPets = petMapper.selectCount(new LambdaQueryWrapper<Pet>()
                .ge(Pet::getCreatedAt, LocalDate.now().atStartOfDay()));
        long todayApps = appMapper.selectCount(new LambdaQueryWrapper<AdoptionApplication>()
                .ge(AdoptionApplication::getCreatedAt, LocalDate.now().atStartOfDay()));

        return R.ok(Map.of(
                "totalUsers", totalUsers, "totalPets", totalPets, "totalAdoptions", totalAdoptions,
                "todayNewUsers", todayUsers, "todayNewPets", todayPets, "todayNewApplications", todayApps));
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private record Paginated<T>(List<T> records, long total, int current, int size) {}
}
