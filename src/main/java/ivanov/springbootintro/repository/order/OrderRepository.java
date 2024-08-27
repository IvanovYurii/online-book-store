package ivanov.springbootintro.repository.order;

import ivanov.springbootintro.model.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "orderItems")
    List<Order> findByUserId(Long userId);

    @EntityGraph(attributePaths = "orderItems")
    Optional<Order> getOrderByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = "orderItems")
    Optional<Order> getOrderById(Long orderId);
}
