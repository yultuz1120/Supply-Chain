package com.aichain.mapper;

import com.aichain.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Yultuz
 */
@Mapper
public interface DishFlavorMapper {
    //批量插入口味数据
    void insertBatch(List<DishFlavor> flavors);
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);
    @Select("select * from dish_flavor where dish_id = #{dishId} ")
    List<DishFlavor> getByDishId(Long dishId);
}
