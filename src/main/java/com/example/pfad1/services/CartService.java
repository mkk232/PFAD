package com.example.pfad1.services;

import com.example.pfad1.entities.cart.CartEntity;
import com.example.pfad1.entities.cart.OrderEntity;
import com.example.pfad1.entities.product.ProductEntity;
import com.example.pfad1.entities.user.UserEntity;
import com.example.pfad1.enums.cart.*;
import com.example.pfad1.mappers.ICartMapper;
import com.example.pfad1.vos.cart.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CartService {
    private static class RegExp {
        public static final String PRODUCT_INDEX = "^([0-9]{0,3})$";
        public static final String STOCK = "^([0-9]{0,3})$";
        public static final String ORDER_CODE = "^([0-9]{0,})$";
    }

    private final ICartMapper cartMapper;

    @Autowired
    public CartService(ICartMapper cartMapper) {
        this.cartMapper = cartMapper;
    }

    public static boolean checkProductIndex(String s) {
        return s != null && s.matches(RegExp.PRODUCT_INDEX);
    }

    public static boolean checkStock(String s) {
        return s != null && s.matches(RegExp.STOCK);
    }

    public static boolean checkOrderCode(String s) {
        return s != null && s.matches(RegExp.ORDER_CODE);
    }

    public void read(CartReadVo cartReadVo, UserEntity userEntity) {
        if (userEntity == null) {
            cartReadVo.setResult(CartReadResult.NOT_ALLOWED);
            return;
        }
        cartReadVo.setUserId(userEntity.getId());
        CartReadVo[] cartReadVos = this.cartMapper.selectCarts(cartReadVo);
        if(cartReadVos == null || this.cartMapper.selectCountCarts(cartReadVo) == 0) {
            cartReadVo.setResult(CartReadResult.CART_NOT_DEFINED);
            return;
        }
        cartReadVo.setCartReadVos(cartReadVos);
        cartReadVo.setCartCount(this.cartMapper.selectCountCarts(cartReadVo));
        cartReadVo.setResult(CartReadResult.SUCCESS);
    }

    public void delete(CartDeleteVo cartDeleteVo, UserEntity userEntity) {
        if (!CartService.checkProductIndex(String.valueOf(cartDeleteVo.getProductIndex()))) {
            cartDeleteVo.setResult(CartDeleteResult.NORMALIZATION_FAILURE);
            return;
        }
        if (userEntity == null) {
            cartDeleteVo.setResult(CartDeleteResult.NOT_ALLOWED);
            return;
        }
        cartDeleteVo.setUserId(userEntity.getId());
//        int deleteCart = this.cartMapper.deleteCart(cartDeleteVo);
        if (this.cartMapper.deleteCart(cartDeleteVo) == 0) {
            cartDeleteVo.setResult(CartDeleteResult.CART_NOT_DEFINED);
            return;
        } else {
            cartDeleteVo.setResult(CartDeleteResult.SUCCESS);
        }
    }

    public void deleteAll(CartDeleteVo cartDeleteVo, UserEntity userEntity) {
        if (userEntity == null) {
            cartDeleteVo.setResult(CartDeleteResult.NOT_ALLOWED);
            return;
        }
        cartDeleteVo.setUserId(userEntity.getId());
        this.cartMapper.deleteCartAll(cartDeleteVo);
        cartDeleteVo.setResult(CartDeleteResult.SUCCESS);
    }

    public void update(CartUpdateVo cartUpdateVo, UserEntity userEntity) {
        for (int i = 0; i < cartUpdateVo.getProductsIndex().length; i++) {
            if (!CartService.checkProductIndex(String.valueOf(cartUpdateVo.getProductsIndex()[i])) ||
                    !CartService.checkStock(String.valueOf(cartUpdateVo.getStocks()[i]))) {
                cartUpdateVo.setResult(CartUpdateResult.NORMALIZATION_FAILURE);
                return;
            }
        }
        if (userEntity == null) {
            cartUpdateVo.setResult(CartUpdateResult.NOT_ALLOWED);
            return;
        }
        cartUpdateVo.setUserId(userEntity.getId());
        for (int i = 0; i < cartUpdateVo.getProductsIndex().length; i++) {
            cartUpdateVo.setProductIndex(cartUpdateVo.getProductsIndex()[i]);
            cartUpdateVo.setStock(cartUpdateVo.getStocks()[i]);
            this.cartMapper.updateCart(cartUpdateVo);
        }
        cartUpdateVo.setResult(CartUpdateResult.SUCCESS);
    }

    public void order(CartReadVo cartReadVo, UserEntity userEntity) {
        if (userEntity == null) {
            cartReadVo.setResult(CartReadResult.NOT_ALLOWED);
            return;
        }
        // db??? ?????? ?????? ??????
        // order ???????????? ???????????? user, product, cart ?????? ????????????
        // ???????????? db ?????????
        // product ???????????? ???????????? ????????? ???????????? product?????? stock ??????
        // ????????? db??? model??? view??? ?????????
    }

    public void order(OrderByCartVo orderByCartVo, UserEntity userEntity) {
        if (userEntity == null) {
            orderByCartVo.setResult(OrderByCartResult.NOT_ALLOWED);
            return;
        }
        LocalDate now = LocalDate.now();
        String orderCode = Integer.toString(now.getYear()) + (int)(Math.random() * (99999999 - 10000000 + 1));
        orderByCartVo.setOrderCode(orderCode);
        orderByCartVo.setUserId(userEntity.getId());
        orderByCartVo.setName(userEntity.getName());
        orderByCartVo.setAddressPostal(userEntity.getAddressPostal());
        orderByCartVo.setAddressPrimary(userEntity.getAddressPrimary());
        orderByCartVo.setAddressSecondary(userEntity.getAddressSecondary());
        orderByCartVo.setContactFirst(userEntity.getContactFirst());
        orderByCartVo.setContactSecond(userEntity.getContactSecond());
        orderByCartVo.setContactThird(userEntity.getContactThird());
        CartReadVo[] cartReadVo = cartMapper.selectCarts(orderByCartVo);
        if(cartReadVo == null) {
            orderByCartVo.setResult(OrderByCartResult.CART_NOT_DEFINED);
            return;
        }
        for(int i = 0; i < cartReadVo.length; i++) {
            orderByCartVo.setThumbnail(cartReadVo[i].getThumbnail());
            orderByCartVo.setProductIndex(cartReadVo[i].getProductIndex());
            orderByCartVo.setStock(cartReadVo[i].getStock());
            orderByCartVo.setPrice(cartReadVo[i].getPrice());
            orderByCartVo.setProductName(cartReadVo[i].getProductName());
            this.cartMapper.insertOrder(orderByCartVo);
            this.cartMapper.updateProductStock(orderByCartVo);
        }
        this.cartMapper.deleteCartAll(orderByCartVo);
        orderByCartVo.setResult(OrderByCartResult.SUCCESS);
    }

    public void orderComplete(OrderCompleteVo orderCompleteVo, UserEntity userEntity) {
        orderCompleteVo.setUserId(userEntity.getId());
        OrderEntity orderEntity = this.cartMapper.selectOrder(orderCompleteVo);
        orderCompleteVo.setOrderCode(orderEntity.getOrderCode());
        orderCompleteVo.setName(orderEntity.getName());
        orderCompleteVo.setAddressPostal(orderEntity.getAddressPostal());
        orderCompleteVo.setAddressPrimary(orderEntity.getAddressPrimary());
        orderCompleteVo.setAddressSecondary(orderEntity.getAddressSecondary());
        orderCompleteVo.setContactFirst(orderEntity.getContactFirst());
        orderCompleteVo.setContactSecond(orderEntity.getContactSecond());
        orderCompleteVo.setContactThird(orderEntity.getContactThird());
        orderCompleteVo.setCreatedAt(orderEntity.getCreatedAt());
    }

    public void orderList(OrderListVo orderListVo, UserEntity userEntity) {
        if(userEntity == null) {
            orderListVo.setResult(OrderListResult.NOT_ALLOWED);
            return;
        }
        orderListVo.setUserId(userEntity.getId());
        OrderEntity[] orderEntities = this.cartMapper.selectOrders(orderListVo);
        if(orderEntities == null) {
            orderListVo.setResult(OrderListResult.ORDER_NOT_DEFINED);
            return;
        }
        orderListVo.setCreatedAt(orderEntities[0].getCreatedAt());
        orderListVo.setOrderEntities(orderEntities);
        orderListVo.setResult(OrderListResult.SUCCESS);
    }

    public void orderDelete(OrderDeleteVo orderDeleteVo, UserEntity userEntity) {
        if(!CartService.checkProductIndex(String.valueOf(orderDeleteVo.getProductIndex())) ||
        !CartService.checkOrderCode(orderDeleteVo.getOrderCode())) {
            orderDeleteVo.setResult(OrderDeleteResult.NORMALIZATION_FAILURE);
            return;
        }

        if(userEntity == null) {
           orderDeleteVo.setResult(OrderDeleteResult.NOT_ALLOWED);
           return;
        }

        orderDeleteVo.setUserId(userEntity.getId());
        if(this.cartMapper.deleteOrder(orderDeleteVo) == 0) {
            orderDeleteVo.setResult(OrderDeleteResult.ORDER_NOT_DEFINED);
            return;
        }

        orderDeleteVo.setResult(OrderDeleteResult.SUCCESS);
    }
}
