package ivanov.springbootintro.repository.book;

import ivanov.springbootintro.model.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

public interface BookRepository extends JpaRepository<Book, Long>,
        JpaSpecificationExecutor<Book> {

    @Override
    @Query(value = "FROM Book b WHERE b.id = :id AND b.isDeleted = false")
    Optional<Book> findById(@NonNull Long id);

    @Override
    @Query(value = "FROM Book b WHERE b.isDeleted = false")
    @EntityGraph(attributePaths = "categories")
    Page<Book> findAll(@NonNull Pageable pageable);

    @Override
    @Modifying
    @Query(value = "UPDATE Book b SET b.isDeleted = true WHERE b.id = :id")
    void deleteById(@NonNull Long id);

    Optional<Book> findByIsbn(String isbn);

    List<Book> findAllByCategoriesId(Long id);

    Long countByIsDeletedFalse();
}
