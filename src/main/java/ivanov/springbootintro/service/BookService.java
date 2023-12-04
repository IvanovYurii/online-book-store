package ivanov.springbootintro.service;

import ivanov.springbootintro.dto.book.BookDto;
import ivanov.springbootintro.dto.book.BookSearchParameters;
import ivanov.springbootintro.dto.book.CreateBookRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDto createBook(CreateBookRequestDto requestDto);

    List<BookDto> findAllBook(Pageable pageable);

    BookDto findBookById(Long id);

    BookDto updateBookById(CreateBookRequestDto requestDto, Long id);

    void deleteBookById(Long id);

    List<BookDto> search(BookSearchParameters searchParameters, Pageable pageable);
}
