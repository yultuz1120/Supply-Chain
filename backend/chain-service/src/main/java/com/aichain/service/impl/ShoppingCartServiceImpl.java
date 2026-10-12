package com.aichain.service.impl;

import com.aichain.context.BaseContext;
import com.aichain.dto.ShoppingCartDTO;
import com.aichain.entity.Dish;
import com.aichain.entity.Setmeal;
import com.aichain.entity.ShoppingCart;
import com.aichain.mapper.DishMapper;
import com.aichain.mapper.SetmealMapper;
import com.aichain.mapper.ShoppingCartMapper;
import com.aichain.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author Yultuz
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断购物车商品是否存在 查询user_id和套餐或者菜品id 菜品口味
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //存在 数量加一
        if(list != null && list.size() == 1) {
            ShoppingCart cart = list.get(0);//查到的第一条数据
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        }else{
            //不存在 插入数据 查询到底是套餐呢还说菜品，并对应查询name、price image
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId != null) {
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else{
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        return shoppingCartMapper.list(ShoppingCart.
                                        builder().
                                        userId(BaseContext.getCurrentId()).
                                        build());
    }

    @Override
    public void cleanShoppingCart() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }
}
