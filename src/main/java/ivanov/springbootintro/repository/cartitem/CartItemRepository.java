package ivanov.springbootintro.repository.cartitem;

import ivanov.springbootintro.model.CartItem;
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
}
