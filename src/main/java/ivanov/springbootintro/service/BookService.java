package ivanov.springbootintro.service;

import ivanov.springbootintro.dto.BookDto;
import ivanov.springbootintro.dto.BookSearchParameters;
import ivanov.springbootintro.dto.CreateBookRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDto save(CreateBookRequestDto requestDto);

    List<BookDto> findAll(Pageable pageable);

    BookDto findById(Long id);

    BookDto updateById(CreateBookRequestDto requestDto, Long id);

    void deleteById(Long id);

    List<BookDto> search(BookSearchParameters searchParameters);
}
