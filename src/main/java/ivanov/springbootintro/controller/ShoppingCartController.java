package ivanov.springbootintro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ivanov.springbootintro.dto.cartitem.AddCartItemRequestDto;
import ivanov.springbootintro.dto.cartitem.CartItemDto;
import ivanov.springbootintro.dto.cartitem.UpdateCartItemQuantityBookRequestDto;
import ivanov.springbootintro.dto.shoppingcart.ShoppingCartDto;
import ivanov.springbootintro.model.User;
import ivanov.springbootintro.service.ShoppingCartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Shopping Cart management", description = "Endpoints for managing Shopping Cart")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @GetMapping
    @Operation(
            summary = "Retrieve user's shopping cart",
            description = "Retrieve the shopping cart for the authenticated user. "
                    + "This endpoint requires authentication and returns detailed information "
                    + "about the user's shopping cart, including cart items with book details, "
                    + "quantities, and total cost."
                    + "You can use the 'page' and 'size' query parameters to paginate through the"
                    + " results."
    )
    public ShoppingCartDto getUserShoppingCart(
            Authentication authentication,
            @ParameterObject Pageable pageable
    ) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.getUserShoppingCart(user, pageable);
    }

    @PostMapping
    @Operation(
            summary = "Add book to the shopping cart",
            description = "Add a book to the user's shopping cart. "
                    + "This endpoint requires authentication, and the request should include"
                    + " valid information about the book to be added. The response includes "
                    + "details about the added cart item, such as cart item ID, book details,"
                    + " quantity, and total cost."
    )
    public CartItemDto addItemToShoppingCart(
            Authentication authentication,
            @RequestBody @Valid AddCartItemRequestDto requestDto
    ) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.addItemToShoppingCart(user, requestDto);
    }

    @PutMapping("/cart-items/{cartItemId}")
    @Operation(
            summary = "Update quantity",
            description = "Update the quantity of a specific book in the user's shopping cart. "
                    + "This endpoint requires authentication, and the request should include the"
                    + " updated quantity. The response includes details about the updated cart "
                    + "item, such as cart item ID, book details, and quantity."
    )
    public CartItemDto updateCartItemQuantity(
            Authentication authentication,
            @PathVariable @Min(1) Long cartItemId,
            @RequestBody @Valid UpdateCartItemQuantityBookRequestDto requestDto
    ) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.updateCartItemQuantity(user, cartItemId, requestDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/cart-items/{cartItemId}")
    @Operation(summary = "Remove a book from the shopping cart",
            description = "Remove a specific book from the user's shopping cart. "
                    + "This endpoint requires authentication and removes the specified book "
                    + "from the shopping cart. The response returns no content with an HTTP "
                    + "status of 204 indicating a successful removal."
    )
    public void deleteBookFromShoppingCart(
            Authentication authentication,
            @PathVariable @Min(1) Long cartItemId
    ) {
        User user = (User) authentication.getPrincipal();
        shoppingCartService.deleteBookFromShoppingCart(user, cartItemId);
    }
}
