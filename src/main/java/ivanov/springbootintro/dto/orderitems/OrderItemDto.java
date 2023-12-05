package ivanov.springbootintro.dto.orderitems;

public record OrderItemDto(
        Long id,
        Long bookId,
        int quantity
) {
}
