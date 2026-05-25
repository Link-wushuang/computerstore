// CartController.java
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.CartService;
import com.example.demo.vo.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    // 获取当前用户ID（实际项目中从Session或Token获取）
    private Long getCurrentUserId(HttpSession session) {
        // 模拟获取用户ID，实际应从session或JWT中获取
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            // 测试用，生产环境应返回401
            return 1L;
        }
        return userId;
    }
    
    @GetMapping("/list")
    public Result<List<CartItemVO>> getCartList(HttpSession session) {
        Long userId = getCurrentUserId(session);
        List<CartItemVO> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }
    
    @PutMapping("/quantity/{cartId}")
    public Result<?> updateQuantity(@PathVariable Long cartId, 
                                    @RequestParam Integer quantity,
                                    HttpSession session) {
        if (quantity == null || quantity < 1) {
            return Result.error("数量必须大于0");
        }
        Long userId = getCurrentUserId(session);
        boolean success = cartService.updateQuantity(userId, cartId, quantity);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败，请重试");
        }
    }
    
    @DeleteMapping("/items")
    public Result<?> deleteCartItems(@RequestBody Map<String, List<Long>> params, HttpSession session) {
        List<Long> cartIds = params.get("cartIds");
        if (cartIds == null || cartIds.isEmpty()) {
            return Result.error("请选择要删除的商品");
        }
        Long userId = getCurrentUserId(session);
        boolean success = cartService.deleteCartItems(userId, cartIds);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}

// FavoritesController.java
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.FavoritesService;
import com.example.demo.vo.FavoriteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {
    
    @Autowired
    private FavoritesService favoritesService;
    
    private Long getCurrentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return 1L;
        }
        return userId;
    }
    
    @GetMapping("/list")
    public Result<List<FavoriteVO>> getFavoritesList(HttpSession session) {
        Long userId = getCurrentUserId(session);
        List<FavoriteVO> favorites = favoritesService.getFavoritesList(userId);
        return Result.success(favorites);
    }
    
    @PostMapping("/add-to-cart")
    public Result<?> addToCartFromFavorites(@RequestBody Map<String, List<Long>> params, HttpSession session) {
        List<Long> favoriteIds = params.get("favoriteIds");
        if (favoriteIds == null || favoriteIds.isEmpty()) {
            return Result.error("请选择要加入购物车的商品");
        }
        Long userId = getCurrentUserId(session);
        boolean success = favoritesService.addToCartFromFavorites(userId, favoriteIds);
        if (success) {
            return Result.success("成功加入购物车");
        } else {
            return Result.error("加入购物车失败");
        }
    }
    
    @DeleteMapping("/items")
    public Result<?> deleteFavorites(@RequestBody Map<String, List<Long>> params, HttpSession session) {
        List<Long> favoriteIds = params.get("favoriteIds");
        if (favoriteIds == null || favoriteIds.isEmpty()) {
            return Result.error("请选择要删除的收藏");
        }
        Long userId = getCurrentUserId(session);
        boolean success = favoritesService.deleteFavorites(userId, favoriteIds);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}
