// CartMapper.java
package com.example.demo.mapper;

import com.example.demo.entity.Cart;
import com.example.demo.vo.CartItemVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartMapper {
    
    @Select("SELECT * FROM cart WHERE user_id = #{userId} ORDER BY create_time DESC")
    @Results(id = "cartResultMap", value = {
        @Result(property = "cartId", column = "cart_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "productId", column = "product_id"),
        @Result(property = "quantity", column = "quantity"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    List<Cart> findByUserId(Long userId);
    
    @Select("SELECT c.cart_id, c.product_id, c.quantity, p.product_name, p.price, p.product_image, p.stock " +
            "FROM cart c JOIN product p ON c.product_id = p.product_id " +
            "WHERE c.user_id = #{userId} ORDER BY c.create_time DESC")
    @Results({
        @Result(property = "cartId", column = "cart_id"),
        @Result(property = "productId", column = "product_id"),
        @Result(property = "productName", column = "product_name"),
        @Result(property = "price", column = "price"),
        @Result(property = "productImage", column = "product_image"),
        @Result(property = "quantity", column = "quantity"),
        @Result(property = "stock", column = "stock"),
        @Result(property = "subtotal", column = "price", expression = "java(new java.math.BigDecimal(java.util.Optional.ofNullable(quantity).orElse(0)).multiply(price))")
    })
    List<CartItemVO> findCartItemsByUserId(Long userId);
    
    @Select("SELECT * FROM cart WHERE cart_id = #{cartId} AND user_id = #{userId}")
    Cart findByCartIdAndUserId(@Param("cartId") Long cartId, @Param("userId") Long userId);
    
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND product_id = #{productId}")
    Cart findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    @Insert("INSERT INTO cart(user_id, product_id, quantity, create_time, update_time) " +
            "VALUES(#{userId}, #{productId}, #{quantity}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "cartId")
    int insert(Cart cart);
    
    @Update("UPDATE cart SET quantity = #{quantity}, update_time = NOW() WHERE cart_id = #{cartId} AND user_id = #{userId}")
    int updateQuantity(@Param("cartId") Long cartId, @Param("quantity") Integer quantity, @Param("userId") Long userId);
    
    @Delete("DELETE FROM cart WHERE cart_id = #{cartId} AND user_id = #{userId}")
    int deleteByCartIdAndUserId(@Param("cartId") Long cartId, @Param("userId") Long userId);
    
    @Delete("<script>" +
            "DELETE FROM cart WHERE user_id = #{userId} AND cart_id IN " +
            "<foreach collection='cartIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int batchDelete(@Param("userId") Long userId, @Param("cartIds") List<Long> cartIds);
}

// FavoritesMapper.java
package com.example.demo.mapper;

import com.example.demo.entity.Favorite;
import com.example.demo.vo.FavoriteVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoritesMapper {
    
    @Select("SELECT f.favorite_id, f.product_id, p.product_name, p.price, p.product_image, p.stock " +
            "FROM favorites f JOIN product p ON f.product_id = p.product_id " +
            "WHERE f.user_id = #{userId} ORDER BY f.create_time DESC")
    @Results({
        @Result(property = "favoriteId", column = "favorite_id"),
        @Result(property = "productId", column = "product_id"),
        @Result(property = "productName", column = "product_name"),
        @Result(property = "price", column = "price"),
        @Result(property = "productImage", column = "product_image"),
        @Result(property = "stock", column = "stock")
    })
    List<FavoriteVO> findFavoritesByUserId(Long userId);
    
    @Select("SELECT * FROM favorites WHERE user_id = #{userId} AND product_id = #{productId}")
    Favorite findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
    
    @Insert("INSERT INTO favorites(user_id, product_id, create_time) VALUES(#{userId}, #{productId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "favoriteId")
    int insert(Favorite favorite);
    
    @Delete("DELETE FROM favorites WHERE favorite_id = #{favoriteId} AND user_id = #{userId}")
    int deleteByFavoriteIdAndUserId(@Param("favoriteId") Long favoriteId, @Param("userId") Long userId);
    
    @Delete("<script>" +
            "DELETE FROM favorites WHERE user_id = #{userId} AND favorite_id IN " +
            "<foreach collection='favoriteIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int batchDelete(@Param("userId") Long userId, @Param("favoriteIds") List<Long> favoriteIds);
}
