package com.aichain.mapper;

import com.aichain.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Yultuz
 */
@Mapper
public interface SetmealDishMapper {
    //select setmeal id from setmeal_dish where dish_id in (1,2,3,4)
    List<Long> getSetmealDishIdsByDishId(List<Long> dishIds);

    void insertBatch(List<SetmealDish> setmealDishes);
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long id);
}
