package ivanov.springbootintro.dto.order;

import jakarta.validation.constraints.NotEmpty;

public record PlaceOrderRequestDto(
        @NotEmpty
        String shippingAddress
) {
}
