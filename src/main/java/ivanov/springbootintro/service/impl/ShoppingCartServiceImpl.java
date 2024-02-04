package ivanov.springbootintro.service.impl;

import ivanov.springbootintro.dto.cartitem.AddCartItemRequestDto;
import ivanov.springbootintro.dto.cartitem.CartItemDto;
import ivanov.springbootintro.dto.cartitem.UpdateCartItemQuantityBookRequestDto;
import ivanov.springbootintro.dto.shoppingcart.ShoppingCartDto;
import ivanov.springbootintro.exception.EntityNotFoundException;
import ivanov.springbootintro.mapper.CartItemMapper;
import ivanov.springbootintro.mapper.ShoppingCartMapper;
import ivanov.springbootintro.model.Book;
import ivanov.springbootintro.model.CartItem;
import ivanov.springbootintro.model.ShoppingCart;
import ivanov.springbootintro.model.User;
import ivanov.springbootintro.repository.book.BookRepository;
import ivanov.springbootintro.repository.cartitem.CartItemRepository;
import ivanov.springbootintro.repository.shoppingcart.ShoppingCartRepository;
import ivanov.springbootintro.service.ShoppingCartService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final CartItemMapper cartItemMapper;
    private final BookRepository bookRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public ShoppingCartDto getUserShoppingCart(User user, Pageable pageable) {
        ShoppingCart shoppingCart = shoppingCartRepository
                .getShoppingCartByUserEmail(user.getEmail());
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public CartItemDto addItemToShoppingCart(User user, AddCartItemRequestDto requestDto) {
        Book book = bookRepository.findById(requestDto.bookId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id="
                        + requestDto.bookId()));
        ShoppingCart shoppingCart = shoppingCartRepository
                .getShoppingCartByUserEmail(user.getEmail());
        Optional<CartItem> cartItemWithPresentBook = cartItemRepository
                .getCartItemByBookIdAndShoppingCartId(requestDto.bookId(), shoppingCart.getId());
        CartItem cartItem;
        if (cartItemWithPresentBook.isPresent()) {
            cartItem = cartItemWithPresentBook.get();
            cartItem.setQuantity(cartItemWithPresentBook.get().getQuantity()
                    + requestDto.quantity());
        } else {
            cartItem = cartItemMapper.toEntity(requestDto);
            cartItem.setShoppingCart(shoppingCart);
            cartItem.setBook(book);
        }
        return cartItemMapper.toDto(cartItemRepository.save(cartItem));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public CartItemDto updateCartItemQuantity(
            User user,
            Long cartItemId,
            UpdateCartItemQuantityBookRequestDto updateDto) {
        CartItem cartItem = findCartItemInShoppingCart(user.getEmail(), cartItemId, "update");
        cartItem.setQuantity(updateDto.quantity());
        cartItemRepository.save(cartItem);
        return cartItemMapper.toDto(cartItem);
    }

    @Override
    public void deleteBookFromShoppingCart(User user, Long cartItemId) {
        CartItem cartItem = findCartItemInShoppingCart(user.getEmail(), cartItemId, "delete");
        cartItemRepository.delete(cartItem);
    }

    private CartItem findCartItemInShoppingCart(
            String userEmail, Long cartItemId, String operationName) {
        ShoppingCart shoppingCart = shoppingCartRepository
                .getShoppingCartByUserEmail(userEmail);
        return cartItemRepository
                .getCartItemByIdAndShoppingCartId(cartItemId, shoppingCart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Can't " + operationName
                        + " Shopping Cart: CartItem with ID " + cartItemId + " not found."));
    }
}
