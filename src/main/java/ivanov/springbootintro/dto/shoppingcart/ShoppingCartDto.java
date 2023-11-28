package ivanov.springbootintro.dto.shoppingcart;

import ivanov.springbootintro.dto.cartitem.CartItemDto;
import java.util.List;

public record ShoppingCartDto(Long id, Long userId, List<CartItemDto> cartItems) {
}
