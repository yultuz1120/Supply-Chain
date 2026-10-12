package com.aichain.mapper;

import com.github.pagehelper.Page;
import com.aichain.annotation.AutoFill;
import com.aichain.dto.SetmealPageQueryDTO;
import com.aichain.entity.Setmeal;
import com.aichain.enumeration.OperationType;
import com.aichain.vo.DishItemVO;
import com.aichain.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);
    @Delete("delete from setmeal where id = #{id}")
    void deleteById(Long setmealId);
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    List<Setmeal> list(Setmeal setmeal);
    /**
     * 根据套餐id查询菜品选项
     * @param_setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long id);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
