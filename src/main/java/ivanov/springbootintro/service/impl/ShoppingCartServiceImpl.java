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
import java.util.Set;
import java.util.stream.Collectors;
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
        Set<Long> bookIds = shoppingCart.getCartItems().stream()
                .map(cartItem -> cartItem.getBook().getId())
                .collect(Collectors.toSet());
        boolean containsBookId = bookIds.contains(requestDto.bookId());
        CartItem cartItem = cartItemMapper.toEntity(requestDto);
        if (containsBookId) {
            CartItem existingCartItem = findCartItemInShoppingCart(
                    shoppingCart, book.getId());
            existingCartItem.setQuantity(existingCartItem.getQuantity()
                    + requestDto.quantity());
            cartItem = existingCartItem;
        } else {
            cartItem.setQuantity(requestDto.quantity());
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
        ShoppingCart shoppingCart = shoppingCartRepository
                .getShoppingCartByUserEmail(user.getEmail());
        CartItem cartItem = findCartItemInShoppingCart(shoppingCart, cartItemId);
        cartItem.setQuantity(updateDto.quantity());
        cartItemRepository.save(cartItem);
        return cartItemMapper.toDto(cartItem);
    }

    @Override
    public void deleteBookFromShoppingCart(User user, Long cartItemId) {
        ShoppingCart shoppingCart = shoppingCartRepository
                .getShoppingCartByUserEmail(user.getEmail());
        CartItem cartItem = findCartItemInShoppingCart(shoppingCart, cartItemId);
        cartItemRepository.delete(cartItem);
    }

    private CartItem findCartItemInShoppingCart(
            ShoppingCart shoppingCart, Long bookId) {
        return shoppingCart.getCartItems()
                .stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("CartItem not found for bookId: " + bookId));
    }
}
