package com.aichain.mapper;

import com.github.pagehelper.Page;
import com.aichain.annotation.AutoFill;
import com.aichain.dto.DishPageQueryDTO;
import com.aichain.entity.Dish;
import com.aichain.enumeration.OperationType;
import com.aichain.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);
    //菜品表插入1条数据
    @AutoFill(value = OperationType.INSERT )
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);
    @AutoFill(value = OperationType.UPDATE )
    void update(Dish dish);//口味表插入n条数据

    List<Dish> list(Dish dish);

    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long id);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
