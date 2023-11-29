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
    @Operation(summary = "Retrieve user's shopping cart",
            description = "Retrieve user's shopping cart")
    public ShoppingCartDto findAll(Authentication authentication,
                                   @ParameterObject Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.getUserShoppingCart(user, pageable);
    }

    @PostMapping
    @Operation(summary = "Add book to the shopping cart",
            description = "Add book to the user's shopping cart")
    public CartItemDto addItemToShoppingCart(
            Authentication authentication,
            @RequestBody @Valid AddCartItemRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.addBookToShoppingCart(user, requestDto);
    }

    @PutMapping("/cart-items/{cartItemId}")
    @Operation(summary = "Update quantity",
            description = "Update quantity of a book in the shopping cart")
    public CartItemDto updateById(
            Authentication authentication,
            @PathVariable Long cartItemId,
            @RequestBody @Valid UpdateCartItemQuantityBookRequestDto requestDto
    ) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.updateCartItemQuantity(user, cartItemId, requestDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/cart-items/{cartItemId}")
    @Operation(summary = "Remove a book from the shopping cart",
            description = "Remove a book from the shopping cart")
    public void deleteById(Authentication authentication, @PathVariable Long cartItemId) {
        User user = (User) authentication.getPrincipal();
        shoppingCartService.deleteBookFromShoppingCart(user, cartItemId);
    }
}
