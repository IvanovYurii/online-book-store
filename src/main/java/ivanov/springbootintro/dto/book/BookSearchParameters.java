package ivanov.springbootintro.dto.book;

import java.math.BigDecimal;

public record BookSearchParameters(
        String[] titles,
        String[] authors,
        String[] isbn,
        BigDecimal priceFrom,
        BigDecimal priceTo,
        String description,
        String[] categoryIds
) {
}
