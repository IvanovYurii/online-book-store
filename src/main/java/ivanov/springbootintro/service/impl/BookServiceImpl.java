package ivanov.springbootintro.service.impl;

import ivanov.springbootintro.dto.BookDto;
import ivanov.springbootintro.dto.BookSearchParameters;
import ivanov.springbootintro.dto.CreateBookRequestDto;
import ivanov.springbootintro.exception.EntityNotFoundException;
import ivanov.springbootintro.mapper.BookMapper;
import ivanov.springbootintro.model.Book;
import ivanov.springbootintro.repository.book.BookRepository;
import ivanov.springbootintro.repository.book.BookSpecificationBuilder;
import ivanov.springbootintro.service.BookService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder bookSpecificationBuilder;

    @Override
    public BookDto save(CreateBookRequestDto requestDto) {
        Book book = bookMapper.toModel(requestDto);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public List<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public BookDto findById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id=" + id));
        return bookMapper.toDto(book);
    }

    @Override
    public BookDto updateById(CreateBookRequestDto requestDto, Long id) {
        Book book = bookMapper.toModel(requestDto);
        book.setId(id);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public List<BookDto> search(BookSearchParameters searchParameters) {
        Specification<Book> build = bookSpecificationBuilder.build(searchParameters);
        return bookRepository.findAll(build)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }
}
