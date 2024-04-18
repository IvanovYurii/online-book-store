package ivanov.springbootintro.repository.book.spec;

import ivanov.springbootintro.model.Book;
import ivanov.springbootintro.repository.SpecificationProvider;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class CategoriesSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "categories";
    }

    @Override
    public Specification<Book> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) ->
                root.join(this.getKey()).get("id").in(Arrays.stream(params)
                        .map(Long::valueOf)
                        .toArray());
    }
}
