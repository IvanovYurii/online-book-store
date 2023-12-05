package ivanov.springbootintro.dto.order;

import ivanov.springbootintro.dto.orderitems.OrderItemDto;
import ivanov.springbootintro.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record OrderDto(
        Long id,
        Long userId,
        Set<OrderItemDto> orderItems,
        LocalDateTime orderDate,
        BigDecimal total,
        Order.Status status
) {
}
