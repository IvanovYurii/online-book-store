package ivanov.springbootintro.service.impl;

import ivanov.springbootintro.dto.book.BookDto;
import ivanov.springbootintro.dto.book.BookSearchParameters;
import ivanov.springbootintro.dto.book.CreateBookRequestDto;
import ivanov.springbootintro.exception.EntityAlreadyPresentException;
import ivanov.springbootintro.exception.EntityNotFoundException;
import ivanov.springbootintro.mapper.BookMapper;
import ivanov.springbootintro.model.Book;
import ivanov.springbootintro.repository.book.BookRepository;
import ivanov.springbootintro.repository.book.BookSpecificationBuilder;
import ivanov.springbootintro.service.BookService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

    private void validateIsbnUniqueness(String isbn, Long id) {
        Optional<Book> bookWithSameIsbn = bookRepository.findByIsbn(isbn);
        if (bookWithSameIsbn.isPresent() && !bookWithSameIsbn.get().getId().equals(id)) {
            throw new EntityAlreadyPresentException("Book with ISBN "
                    + isbn + " is already present");
        }
    }

    @Override
    public BookDto createBook(CreateBookRequestDto requestDto) {
        if (bookRepository.findByIsbn(requestDto.isbn()).isPresent()) {
            throw new EntityAlreadyPresentException("Book with Isbn " + requestDto.isbn()
                    + " is all ready present");
        }
        Book book = bookMapper.toEntity(requestDto);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public List<BookDto> findAllBook(Pageable pageable) {
        return bookRepository.findAll(pageable).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public BookDto findBookById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id=" + id));
        return bookMapper.toDto(book);
    }

    @Override
    public BookDto updateBookById(CreateBookRequestDto requestDto, Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find book by id=" + id));
        if (requestDto != null) {
            String newIsbn = requestDto.isbn();
            if (newIsbn != null && !newIsbn.equals(book.getIsbn())) {
                validateIsbnUniqueness(newIsbn, id);
                book.setIsbn(newIsbn);
            }
            book.setTitle(requestDto.title() != null && !requestDto.title().isEmpty()
                    ? requestDto.title() : book.getTitle());
            book.setAuthor(requestDto.author() != null && !requestDto.author().isEmpty()
                    ? requestDto.author() : book.getAuthor());
            book.setPrice(
                    requestDto.price() != null
                            ? requestDto.price().max(BigDecimal.ZERO) :
                            book.getPrice()
            );
            book.setDescription(requestDto.description() != null
                    ? requestDto.description() : book.getDescription());
            book.setCoverImage(requestDto.coverImage() != null
                    ? requestDto.coverImage() : book.getCoverImage());
        }
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public void deleteBookById(Long id) {
        bookRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id=" + id));
        bookRepository.deleteById(id);
    }

    @Override
    public List<BookDto> search(BookSearchParameters searchParameters, Pageable pageable) {
        Specification<Book> build = bookSpecificationBuilder.build(searchParameters);
        return bookRepository.findAll(build)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }
}
