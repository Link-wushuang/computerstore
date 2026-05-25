// CartService.java
package com.example.demo.service;

import com.example.demo.vo.CartItemVO;
import java.util.List;

public interface CartService {
    List<CartItemVO> getCartList(Long userId);
    boolean updateQuantity(Long userId, Long cartId, Integer quantity);
    boolean deleteCartItems(Long userId, List<Long> cartIds);
    boolean addToCart(Long userId, Long productId, Integer quantity);
}

// CartServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.entity.Cart;
import com.example.demo.mapper.CartMapper;
import com.example.demo.service.CartService;
import com.example.demo.vo.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    
    @Autowired
    private CartMapper cartMapper;
    
    @Override
    public List<CartItemVO> getCartList(Long userId) {
        List<CartItemVO> cartItems = cartMapper.findCartItemsByUserId(userId);
        if (cartItems != null) {
            for (CartItemVO item : cartItems) {
                item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return cartItems;
    }
    
    @Override
    @Transactional
    public boolean updateQuantity(Long userId, Long cartId, Integer quantity) {
        // 检查库存
        Cart cart = cartMapper.findByCartIdAndUserId(cartId, userId);
        if (cart == null) {
            return false;
        }
        // 实际项目中需要检查商品库存
        int result = cartMapper.updateQuantity(cartId, quantity, userId);
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteCartItems(Long userId, List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            return false;
        }
        int result = cartMapper.batchDelete(userId, cartIds);
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean addToCart(Long userId, Long productId, Integer quantity) {
        // 检查是否已存在
        Cart existingCart = cartMapper.findByUserIdAndProductId(userId, productId);
        if (existingCart != null) {
            // 增加数量
            int newQuantity = existingCart.getQuantity() + quantity;
            return updateQuantity(userId, existingCart.getCartId(), newQuantity);
        } else {
            // 新增
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            int result = cartMapper.insert(cart);
            return result > 0;
        }
    }
}

// FavoritesService.java
package com.example.demo.service;

import com.example.demo.vo.FavoriteVO;
import java.util.List;

public interface FavoritesService {
    List<FavoriteVO> getFavoritesList(Long userId);
    boolean deleteFavorites(Long userId, List<Long> favoriteIds);
    boolean addToCartFromFavorites(Long userId, List<Long> favoriteIds);
}

// FavoritesServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.entity.Favorite;
import com.example.demo.mapper.FavoritesMapper;
import com.example.demo.service.CartService;
import com.example.demo.service.FavoritesService;
import com.example.demo.vo.FavoriteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoritesServiceImpl implements FavoritesService {
    
    @Autowired
    private FavoritesMapper favoritesMapper;
    
    @Autowired
    private CartService cartService;
    
    @Override
    public List<FavoriteVO> getFavoritesList(Long userId) {
        return favoritesMapper.findFavoritesByUserId(userId);
    }
    
    @Override
    @Transactional
    public boolean deleteFavorites(Long userId, List<Long> favoriteIds) {
        if (favoriteIds == null || favoriteIds.isEmpty()) {
            return false;
        }
        int result = favoritesMapper.batchDelete(userId, favoriteIds);
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean addToCartFromFavorites(Long userId, List<Long> favoriteIds) {
        if (favoriteIds == null || favoriteIds.isEmpty()) {
            return false;
        }
        
        // 获取收藏项对应的商品ID
        List<FavoriteVO> favorites = favoritesMapper.findFavoritesByUserId(userId);
        for (Long favoriteId : favoriteIds) {
            FavoriteVO favorite = favorites.stream()
                    .filter(f -> f.getFavoriteId().equals(favoriteId))
                    .findFirst()
                    .orElse(null);
            if (favorite != null) {
                // 加入购物车，数量为1
                cartService.addToCart(userId, favorite.getProductId(), 1);
            }
        }
        return true;
    }
}
