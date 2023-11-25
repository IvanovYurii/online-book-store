package ivanov.springbootintro.repository.book;

import ivanov.springbootintro.model.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository extends JpaRepository<Book, Long>,
        JpaSpecificationExecutor<Book> {
    Optional<Book> findByIsbn(String isbn);

    List<Book> findAllByCategoriesId(Long id);
}
