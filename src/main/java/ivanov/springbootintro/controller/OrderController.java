package ivanov.springbootintro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ivanov.springbootintro.dto.order.OrderDto;
import ivanov.springbootintro.dto.order.PlaceOrderRequestDto;
import ivanov.springbootintro.dto.order.UpdateStatusOrderRequestDto;
import ivanov.springbootintro.dto.orderitems.OrderItemDto;
import ivanov.springbootintro.model.User;
import ivanov.springbootintro.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order management", description = "Manage orders, including placing, retrieving "
        + "history, updating status, and accessing items.")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place an order.",
            description = "Place an order for the authenticated user and clear the user's shopping"
                    + " cart. This endpoint requires authentication, and the request should "
                    + "include a valid shipping address to be ordered. "
                    + "The response includes the created order with details such"
                    + " as order ID, user ID, order items, order date, total amount and status."
                    + "You can use the 'page' and 'size' query parameters to paginate through the"
                    + " results."
    )
    public OrderDto placeOrder(
            Authentication authentication,
            @RequestBody @Valid PlaceOrderRequestDto requestDto,
            @ParameterObject Pageable pageable
    ) {
        User user = (User) authentication.getPrincipal();
        return orderService.placeOrder(user, requestDto, pageable);
    }

    @GetMapping
    @Operation(summary = "Retrieve user's order history.",
            description = "Retrieve the order history for the authenticated user. "
                    + "This endpoint requires authentication, and the response includes a list "
                    + "of OrderDto objects representing the user's past orders with details such"
                    + " as order ID, user ID, order items, order date, total amount and status."
                    + "You can use the 'page' and 'size' query parameters to paginate through the"
                    + " results."
    )
    public List<OrderDto> getOrderHistory(
            Authentication authentication,
            @ParameterObject Pageable pageable
    ) {
        User user = (User) authentication.getPrincipal();
        return orderService.getOrderHistory(user.getId(), pageable);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    @Operation(summary = "Update order status.",
            description = "Update the status of an order identified by its order ID. "
                    + "Provide the order ID in the request path, and specify the new status in "
                    + "the request body. The response includes the updated OrderDto. "
                    + "This operation requires the user to have the role ADMIN."
    )
    public OrderDto updateOrderStatus(
            @PathVariable @Min(1) Long id,
            @RequestBody UpdateStatusOrderRequestDto request
    ) {
        return orderService.updateOrderStatus(id, request);
    }

    @GetMapping("/{orderId}/items")
    @Operation(summary = "Retrieve all OrderItems for a specific order.",
            description = "Retrieve detailed information about all OrderItems associated with a "
                    + "specific order. This endpoint requires the order ID as a path parameter. "
                    + "The response includes a list of OrderItem objects with information such as"
                    + " ID, bookId, quantity. Only authenticated users can access this endpoint."
                    + "You can use the 'page' and 'size' query parameters to paginate through the"
                    + " results."
    )
    public List<OrderItemDto> getOrderItems(
            @PathVariable @Min(1) Long orderId,
            @ParameterObject Pageable pageable
    ) {
        return orderService.getOrderItems(orderId, pageable);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Retrieve a specific OrderItem within an order",
            description = "Retrieve detailed information about specific OrderItem associated with "
                    + "a specific order. This endpoint requires the order ID, item ID as a path "
                    + "parameter. The response includes OrderItem objects with information such as"
                    + " ID, bookId, quantity. Only authenticated users can access this endpoint."
    )
    public OrderItemDto getOrderItem(
            @PathVariable @Min(1) Long orderId,
            @PathVariable @Min(1) Long itemId
    ) {
        return orderService.getOrderItem(orderId, itemId);
    }
}
