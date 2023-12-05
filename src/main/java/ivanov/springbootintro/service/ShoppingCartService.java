package ivanov.springbootintro.service;

import ivanov.springbootintro.dto.cartitem.AddCartItemRequestDto;
import ivanov.springbootintro.dto.cartitem.CartItemDto;
import ivanov.springbootintro.dto.cartitem.UpdateCartItemQuantityBookRequestDto;
import ivanov.springbootintro.dto.shoppingcart.ShoppingCartDto;
import ivanov.springbootintro.model.User;
import org.springframework.data.domain.Pageable;

public interface ShoppingCartService {
    ShoppingCartDto getUserShoppingCart(User user, Pageable pageable);

    CartItemDto addItemToShoppingCart(User user, AddCartItemRequestDto requestDto);

    CartItemDto updateCartItemQuantity(User user, Long cartItemId,
                                       UpdateCartItemQuantityBookRequestDto updateDto);

    void deleteBookFromShoppingCart(User user, Long cartItemId);
}
