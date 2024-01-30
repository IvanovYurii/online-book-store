package ivanov.springbootintro.dto.book;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateBookRequestDto(
        String title,
        String author,
        String isbn,
        BigDecimal price,
        String description,
        String coverImage,
        Set<Long> categoryIds
) {
}
