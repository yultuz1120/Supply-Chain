package com.aichain.controller.admin;

import com.aichain.dto.DishDTO;
import com.aichain.dto.DishPageQueryDTO;
import com.aichain.entity.Dish;
import com.aichain.result.PageResult;
import com.aichain.result.Result;
import com.aichain.service.DishService;
import com.aichain.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author Yultuz
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        //清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
//        redisTemplate.delete(key);
        cleanCache(key);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询{}", dishPageQueryDTO);
        PageResult paeResult =   dishService.pageQuery(dishPageQueryDTO);
        return Result.success(paeResult);
    }

    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除：{}",ids);
        //将所有的菜品缓存数据清理掉，所有dish_开头的key
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        cleanCache("dish_*");
        dishService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品：{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}",dishDTO);
        //将所有的菜品缓存数据清理掉，所有dish_开头的key
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        cleanCache("dish_*");
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    //"根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

}
