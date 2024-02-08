package ivanov.springbootintro.repository.cartitem;

import ivanov.springbootintro.exception.EntityNotFoundException;
import ivanov.springbootintro.model.CartItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long>,
        JpaSpecificationExecutor<CartItem> {

    @Modifying
    @Query("DELETE FROM CartItem WHERE shoppingCart.id = :shoppingCartId")
    void deleteCartItemsByShoppingCart(@Param("shoppingCartId") Long shoppingCartId);

    @EntityGraph(attributePaths = "book")
    Optional<CartItem> getCartItemByIdAndShoppingCartId(Long cartItemId, Long shoppingCartId);

    @EntityGraph(attributePaths = "book")
    Optional<CartItem> getCartItemByBookIdAndShoppingCartId(Long bookId, Long shoppingCartId);

    boolean existsByIdAndShoppingCartUserId(Long cartItemId, Long userId);

    default void deleteCartItemIfExistsFromUserShoppingCart(Long cartItemId, Long userId) {
        if (existsByIdAndShoppingCartUserId(cartItemId, userId)) {
            deleteById(cartItemId);
        } else {
            throw new EntityNotFoundException("Can't delete Shopping Cart: CartItem with ID "
                    + cartItemId + " not found.");
        }
    }
}
