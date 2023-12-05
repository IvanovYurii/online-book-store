package ivanov.springbootintro.repository.shoppingcart;

import ivanov.springbootintro.model.ShoppingCart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long>,
        JpaSpecificationExecutor<ShoppingCart> {
    @EntityGraph(attributePaths = "cartItems")
    ShoppingCart getShoppingCartByUserEmail(String email);
}
