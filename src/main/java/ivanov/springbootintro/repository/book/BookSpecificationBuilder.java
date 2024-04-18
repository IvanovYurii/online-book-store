package ivanov.springbootintro.repository.book;

import ivanov.springbootintro.dto.book.BookSearchParameters;
import ivanov.springbootintro.model.Book;
import ivanov.springbootintro.repository.SpecificationBuilder;
import ivanov.springbootintro.repository.SpecificationProviderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BookSpecificationBuilder implements SpecificationBuilder<Book> {
    private static final String AUTHOR_KEY = "author";
    private static final String TITLE_KEY = "title";
    private static final String ISBN_KEY = "isbn";
    private static final String PRICE_KEY = "price";
    private static final String DESCRIPTION_KEY = "description";
    private static final String CATEGORY_KEY = "categories";
    private final SpecificationProviderManager<Book> specificationProviderManager;

    @Override
    public Specification<Book> build(BookSearchParameters searchParameters) {
        Specification<Book> spec = Specification.where(null);
        spec = addAuthorsSpecification(spec, searchParameters);
        spec = addTitlesSpecification(spec, searchParameters);
        spec = addIsbnSpecification(spec, searchParameters);
        spec = addPriceSpecification(spec, searchParameters);
        spec = addDescriptionSpecification(spec, searchParameters);
        spec = addCategoryIdsSpecification(spec, searchParameters);
        return spec;
    }

    private Specification<Book> addAuthorsSpecification(
            Specification<Book> spec,
            BookSearchParameters searchParameters) {
        if (searchParameters.authors() != null && searchParameters.authors().length > 0) {
            return spec.and(specificationProviderManager.getSpecificationProvider(AUTHOR_KEY)
                    .getSpecification(searchParameters.authors()));
        }
        return spec;
    }

    private Specification<Book> addTitlesSpecification(
            Specification<Book> spec,
            BookSearchParameters searchParameters) {
        if (searchParameters.titles() != null && searchParameters.titles().length > 0) {
            return spec.and(specificationProviderManager.getSpecificationProvider(TITLE_KEY)
                    .getSpecification(searchParameters.titles()));
        }
        return spec;
    }

    private Specification<Book> addIsbnSpecification(
            Specification<Book> spec,
            BookSearchParameters searchParameters) {
        if (searchParameters.isbn() != null && searchParameters.isbn().length > 0) {
            return spec.and(specificationProviderManager.getSpecificationProvider(ISBN_KEY)
                    .getSpecification(searchParameters.isbn()));
        }
        return spec;
    }

    private Specification<Book> addPriceSpecification(
            Specification<Book> spec,
            BookSearchParameters searchParameters) {
        if (searchParameters.priceFrom() != null
                || searchParameters.priceTo() != null) {
            return spec.and(specificationProviderManager.getSpecificationProvider(PRICE_KEY)
                    .getSpecification(searchParameters.priceFrom(), searchParameters.priceTo()));
        }
        return spec;
    }

    private Specification<Book> addDescriptionSpecification(
            Specification<Book> spec,
            BookSearchParameters searchParameters) {
        if (searchParameters.description() != null
                && !searchParameters.description().isEmpty()) {
            return spec.and(specificationProviderManager.getSpecificationProvider(DESCRIPTION_KEY)
                    .getSpecification(searchParameters.description()));
        }
        return spec;
    }

    private Specification<Book> addCategoryIdsSpecification(
            Specification<Book> spec,
            BookSearchParameters searchParameters) {
        if (searchParameters.categoryIds() != null
                && searchParameters.categoryIds().length > 0) {
            return spec.and(specificationProviderManager.getSpecificationProvider(CATEGORY_KEY)
                    .getSpecification(searchParameters.categoryIds()));
        }
        return spec;
    }
}
