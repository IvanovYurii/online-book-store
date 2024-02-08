package ivanov.springbootintro.dto.book;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BookDto {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private String description;
    private String coverImage;
    private Set<Long> categoryIds;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BookDto bookDto = (BookDto) o;
        return Objects.equals(id, bookDto.id)
                && Objects.equals(title, bookDto.title)
                && Objects.equals(author, bookDto.author)
                && Objects.equals(isbn, bookDto.isbn)
                && Objects.equals(price, bookDto.price)
                && Objects.equals(description, bookDto.description)
                && Objects.equals(coverImage, bookDto.coverImage)
                && Objects.equals(categoryIds, bookDto.categoryIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, author, isbn, price, description, coverImage, categoryIds);
    }
}
