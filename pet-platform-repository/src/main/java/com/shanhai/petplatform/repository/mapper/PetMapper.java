package com.shanhai.petplatform.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanhai.petplatform.repository.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 宠物 Mapper — 包含复杂搜索查询
 *
 * @author PetPlatform Team
 */
@Mapper
public interface PetMapper extends BaseMapper<Pet> {

    /**
     * 多条件动态搜索宠物
     *
     * @param page         分页对象
     * @param breed        品种（可选）
     * @param ageMin       最小月龄（可选）
     * @param ageMax       最大月龄（可选）
     * @param gender       性别（可选）
     * @param neutered     是否绝育（可选）
     * @param healthStatus 健康状态（可选）
     * @param province     所在省（可选）
     * @param city         所在市（可选）
     * @param status       宠物状态（可选）
     * @param sortBy       排序字段（可选: created_at / view_count）
     * @return 分页结果
     */
    Page<Pet> searchPets(Page<Pet> page,
                         @Param("breed") String breed,
                         @Param("ageMin") Integer ageMin,
                         @Param("ageMax") Integer ageMax,
                         @Param("gender") Integer gender,
                         @Param("neutered") Integer neutered,
                         @Param("healthStatus") Integer healthStatus,
                         @Param("province") String province,
                         @Param("city") String city,
                         @Param("status") Integer status,
                         @Param("sortBy") String sortBy);

    /**
     * 热门宠物 — 按浏览量降序
     *
     * @param limit 返回条数
     * @return 热门宠物列表
     */
    List<Pet> selectHotPets(@Param("limit") int limit);

}
