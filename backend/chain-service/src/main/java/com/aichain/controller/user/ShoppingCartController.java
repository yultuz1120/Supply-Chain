package com.aichain.controller.user;

import com.aichain.dto.ShoppingCartDTO;
import com.aichain.entity.ShoppingCart;
import com.aichain.result.Result;
import com.aichain.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Yultuz
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车，商品信息为:{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        List<ShoppingCart> list =  shoppingCartService.showShoppingCart();
        return Result.success(list);
    }
    @DeleteMapping("/clean")
    public Result<String> clean(){
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }

}
