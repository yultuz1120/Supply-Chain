package com.aichain.service;

import com.aichain.dto.DishDTO;
import com.aichain.dto.DishPageQueryDTO;
import com.aichain.entity.Dish;
import com.aichain.result.PageResult;
import com.aichain.vo.DishVO;

import java.util.List;

/**
 * @author Yultuz
 */
public interface DishService {
    public void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    DishVO getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDTO dishDTO);

    List<Dish> list(Long categoryId);

    List<DishVO> listWithFlavor(Dish dish);
}
