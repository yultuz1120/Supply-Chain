package com.aichain.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.aichain.constant.MessageConstant;
import com.aichain.constant.StatusConstant;
import com.aichain.dto.DishDTO;
import com.aichain.dto.DishPageQueryDTO;
import com.aichain.entity.Dish;
import com.aichain.entity.DishFlavor;
import com.aichain.exception.DeletionNotAllowedException;
import com.aichain.mapper.DishFlavorMapper;
import com.aichain.mapper.DishMapper;
import com.aichain.mapper.SetmealDishMapper;
import com.aichain.result.PageResult;
import com.aichain.service.DishService;
import com.aichain.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Yultuz
 */
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Transactional //涉及到两个表的操作，标注事物注解，一成都成

    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        //获取dish的主键值
        Long dishId = dish.getId();
        //得到口味表数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            for (DishFlavor f : flavors) {
                f.setDishId(dishId);
            }
            //向口味表中插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page= dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                //起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //是否关联套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealDishIdsByDishId(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        //删除菜品
        for (Long id : ids) {
            dishMapper.deleteById(id);
            dishFlavorMapper.deleteByDishId(id);
        }
        //删除关联口味
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);//修改菜品基本信息
        dishFlavorMapper.deleteByDishId(dishDTO.getId());//删除原有的口味Id
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            for (DishFlavor f : flavors) {
                f.setDishId(dishDTO.getId());//此时，flavors 列表中的每个口味对象只有名字（如“甜味”），
                // 并没有 dishId，因为菜品还没存入数据库，还没产生 ID。
            }
            //向口味表中插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

}

