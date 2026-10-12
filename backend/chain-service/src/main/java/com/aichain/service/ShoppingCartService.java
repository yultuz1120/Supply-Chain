package com.aichain.service;

import com.aichain.dto.ShoppingCartDTO;
import com.aichain.entity.ShoppingCart;

import java.util.List;

/**
 * @author Yultuz
 */
public interface ShoppingCartService {
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShoppingCart();

    void cleanShoppingCart();
}
