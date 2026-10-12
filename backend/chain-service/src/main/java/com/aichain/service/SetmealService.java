package com.aichain.service;

import com.aichain.dto.SetmealDTO;
import com.aichain.dto.SetmealPageQueryDTO;
import com.aichain.entity.Setmeal;
import com.aichain.result.PageResult;
import com.aichain.vo.DishItemVO;
import com.aichain.vo.SetmealVO;

import java.util.List;

/**
 * @author Yultuz
 */
public interface SetmealService {

    void saveWithDish(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteBatch(List<Long> ids);

    SetmealVO getByIdWithDish(Long id);

    void update(SetmealDTO setmealDTO);

    void startOrStop(Integer status, Long id);

    List<Setmeal> list(Setmeal setmeal);

    List<DishItemVO> getDishItemById(Long id);
}
