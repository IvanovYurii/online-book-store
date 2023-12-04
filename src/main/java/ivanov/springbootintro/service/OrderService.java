package ivanov.springbootintro.service;

import ivanov.springbootintro.dto.order.OrderDto;
import ivanov.springbootintro.dto.order.PlaceOrderRequestDto;
import ivanov.springbootintro.dto.order.UpdateStatusOrderRequestDto;
import ivanov.springbootintro.dto.orderitems.OrderItemDto;
import ivanov.springbootintro.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto placeOrder(User user, PlaceOrderRequestDto requestDto, Pageable pageable);

    List<OrderDto> getOrderHistory(Long userId, Pageable pageable);

    OrderDto updateOrderStatus(Long orderId, UpdateStatusOrderRequestDto request);

    List<OrderItemDto> getOrderItems(Long orderId, Pageable pageable);

    OrderItemDto getOrderItem(Long orderId, Long itemId);
}
