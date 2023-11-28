package ivanov.springbootintro.dto.cartitem;

import jakarta.validation.constraints.Min;

public record UpdateCartItemQuantityBookRequestDto(
        @Min(1) int quantity) {
}
