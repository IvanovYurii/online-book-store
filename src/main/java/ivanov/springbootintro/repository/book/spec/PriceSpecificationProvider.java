package ivanov.springbootintro.repository.book.spec;

import ivanov.springbootintro.model.Book;
import ivanov.springbootintro.repository.SpecificationProvider;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class PriceSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "price";
    }

    @Override
    public Specification<Book> getSpecification(BigDecimal priceFrom, BigDecimal priceTo) {
        if (priceFrom != null && priceTo != null) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(this.getKey()),
                            priceFrom,
                            priceTo);
        } else if (priceFrom != null) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get(this.getKey()),
                            priceFrom);
        } else {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get(this.getKey()),
                            priceTo);
        }
    }
}
