package ivanov.springbootintro.dto.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.Set;

public record CreateBookRequestDto(
        @NotEmpty String title,
        @NotEmpty String author,
        @NotEmpty String isbn,
        @Min(0) BigDecimal price,
        String description,
        String coverImage,
        Set<Long> categoryIds
) {
}
