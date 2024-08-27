package ivanov.springbootintro.service.impl;

import ivanov.springbootintro.dto.order.OrderDto;
import ivanov.springbootintro.dto.order.PlaceOrderRequestDto;
import ivanov.springbootintro.dto.order.UpdateStatusOrderRequestDto;
import ivanov.springbootintro.dto.orderitems.OrderItemDto;
import ivanov.springbootintro.exception.EntityNotFoundException;
import ivanov.springbootintro.mapper.OrderItemMapper;
import ivanov.springbootintro.mapper.OrderMapper;
import ivanov.springbootintro.model.CartItem;
import ivanov.springbootintro.model.Order;
import ivanov.springbootintro.model.OrderItem;
import ivanov.springbootintro.model.ShoppingCart;
import ivanov.springbootintro.model.User;
import ivanov.springbootintro.repository.cartitem.CartItemRepository;
import ivanov.springbootintro.repository.order.OrderRepository;
import ivanov.springbootintro.repository.shoppingcart.ShoppingCartRepository;
import ivanov.springbootintro.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    @Override
    public OrderDto placeOrder(User user, PlaceOrderRequestDto requestDto, Pageable pageable) {
        ShoppingCart shoppingCart = shoppingCartRepository
                .getShoppingCartByUserEmail(user.getEmail());
        if (shoppingCart.getCartItems().isEmpty()) {
            throw new EntityNotFoundException("Shopping Cart with email "
                    + user.getEmail() + " is empty");
        }
        Order order = createOrder(user, requestDto, shoppingCart);
        orderRepository.save(order);
        cartItemRepository.deleteCartItemsByShoppingCart(shoppingCart.getId());
        return orderMapper.orderToOrderDto(order);
    }

    @Override
    public List<OrderDto> getOrderHistory(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(orderMapper::orderToOrderDto)
                .toList();
    }

    @Transactional
    @Override
    public OrderDto updateOrderStatus(Long orderId, UpdateStatusOrderRequestDto request) {
        Order order = orderRepository.getOrderById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Can't find order by id=" + orderId));
        Order.Status newStatus = orderStatusFromRequest(request);
        order.setStatus(newStatus);
        orderRepository.save(order);
        return orderMapper.orderToOrderDto(order);
    }

    @Override
    public List<OrderItemDto> getOrderItems(Long orderId, Pageable pageable) {
        Order order = getOrderFromDb(orderId);
        return order.getOrderItems().stream()
                .map(orderItemMapper::orderItemToDto)
                .toList();
    }

    @Override
    public OrderItemDto getOrderItem(Long orderId, Long itemId) {
        Order order = getOrderFromDb(orderId);
        return order.getOrderItems().stream()
                .filter(orderItem -> orderItem.getId().equals(itemId))
                .findFirst()
                .map(orderItemMapper::orderItemToDto)
                .orElseThrow(() -> new EntityNotFoundException("Can't find order item with id="
                        + itemId + " in order id=" + orderId));
    }

    private Order createOrder(
            User user,
            PlaceOrderRequestDto requestDto,
            ShoppingCart shoppingCart) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(requestDto.shippingAddress());
        order.setStatus(Order.Status.PENDING);
        Set<OrderItem> orderItems = createOrderItems(order, shoppingCart.getCartItems());
        BigDecimal total = calculateTotal(orderItems);
        order.setOrderItems(orderItems);
        order.setTotal(total);
        return order;
    }

    private Set<OrderItem> createOrderItems(Order order, Set<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> createOrderItemFromCartItem(order, cartItem))
                .collect(Collectors.toSet());
    }

    private OrderItem createOrderItemFromCartItem(Order order, CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setBook(cartItem.getBook());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getBook().getPrice());
        return orderItem;
    }

    private BigDecimal calculateTotal(Set<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order.Status orderStatusFromRequest(UpdateStatusOrderRequestDto request) {
        return Order.Status.fromValue(request.status()).orElseThrow(
                () -> new EntityNotFoundException("Invalid status: " + request.status()));
    }

    private Order getOrderFromDb(Long orderId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return orderRepository.getOrderByIdAndUserId(orderId, user.getId()).orElseThrow(
                () -> new EntityNotFoundException("Can't find order by id=" + orderId)
        );
    }
}
