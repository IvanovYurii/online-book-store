package ivanov.springbootintro.repository;

import java.math.BigDecimal;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationProvider<T> {
    String getKey();

    default Specification<T> getSpecification(String[] params) {
        return (root, query, criteriaBuilder) ->
                root.get(this.getKey()).in(Arrays.stream(params)
                        .toArray());
    }

    default Specification<T> getSpecification(String params) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(this.getKey()), "%" + params + "%");
    }

    default Specification<T> getSpecification(BigDecimal beginValue, BigDecimal endValue) {
        return null;
    }
}
