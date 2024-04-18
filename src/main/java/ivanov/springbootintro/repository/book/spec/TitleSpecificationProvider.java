package ivanov.springbootintro.repository.book.spec;

import ivanov.springbootintro.model.Book;
import ivanov.springbootintro.repository.SpecificationProvider;
import org.springframework.stereotype.Component;

@Component
public class TitleSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "title";
    }
}
