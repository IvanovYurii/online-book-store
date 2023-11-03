package ivanov.springbootintro.service;

import ivanov.springbootintro.dto.BookDto;
import ivanov.springbootintro.dto.BookSearchParameters;
import ivanov.springbootintro.dto.CreateBookRequestDto;
import java.util.List;

public interface BookService {
    BookDto save(CreateBookRequestDto requestDto);

    List<BookDto> findAll();

    BookDto findById(Long id);

    BookDto updateById(CreateBookRequestDto requestDto, Long id);

    void deleteById(Long id);

    List<BookDto> search(BookSearchParameters searchParameters);
}
