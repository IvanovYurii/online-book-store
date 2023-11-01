package ivanov.springbootintro.service;

import ivanov.springbootintro.model.Book;
import java.util.List;

public interface BookService {
    Book save(Book book);

    List<Book> findAll();
}
