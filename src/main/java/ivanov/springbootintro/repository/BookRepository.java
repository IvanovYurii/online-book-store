package ivanov.springbootintro.repository;

import ivanov.springbootintro.model.Book;
import java.util.List;

public interface BookRepository {
    Book save(Book book);

    List<Book> findAll();
}
