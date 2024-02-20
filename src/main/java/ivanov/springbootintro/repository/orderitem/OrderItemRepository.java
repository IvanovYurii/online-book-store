package ivanov.springbootintro.repository.orderitem;

import ivanov.springbootintro.model.OrderItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>,
        JpaSpecificationExecutor<OrderItem> {

    Optional<OrderItem> findByIdAndOrderId(Long orderId, Long itemId);
}
