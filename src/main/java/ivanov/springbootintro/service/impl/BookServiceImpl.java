package ivanov.springbootintro.service.impl;

import ivanov.springbootintro.dto.book.BookDto;
import ivanov.springbootintro.dto.book.BookDtoWithoutCategoryIds;
import ivanov.springbootintro.dto.book.BookSearchParameters;
import ivanov.springbootintro.dto.book.CreateBookRequestDto;
import ivanov.springbootintro.dto.book.UpdateBookRequestDto;
import ivanov.springbootintro.exception.EntityAlreadyPresentException;
import ivanov.springbootintro.exception.EntityNotFoundException;
import ivanov.springbootintro.mapper.BookMapper;
import ivanov.springbootintro.model.Book;
import ivanov.springbootintro.model.Category;
import ivanov.springbootintro.repository.book.BookRepository;
import ivanov.springbootintro.repository.book.BookSpecificationBuilder;
import ivanov.springbootintro.repository.category.CategoryRepository;
import ivanov.springbootintro.service.BookService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final CategoryRepository categoryRepository;

    @Override
    public BookDto create(CreateBookRequestDto requestDto) {
        if (bookRepository.findByIsbn(requestDto.isbn()).isPresent()) {
            throw new EntityAlreadyPresentException("Can't create a book. Book with Isbn "
                    + requestDto.isbn() + " is already present");
        }
        Book book = new Book();
        if (Objects.nonNull(requestDto.categoryIds()) && !requestDto.categoryIds().isEmpty()) {
            Set<Category> validCategories = validateCategoryIdsExistence(requestDto.categoryIds());
            book.setCategories(validCategories);
        } else {
            book.setCategories(Collections.emptySet());
        }
        book = bookMapper.toEntity(requestDto);
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
        Book book = assertBookExistsById(id);
        return bookMapper.toDto(book);
    }

    @Override
    public BookDto updateById(UpdateBookRequestDto requestDto, Long id) {
        Book book = assertBookExistsById(id);
        if (requestDto.isbn() != null && !requestDto.isbn().isEmpty()) {
            validateIsbnUniqueness(requestDto.isbn(), id);
            book.setIsbn(requestDto.isbn());
        } else {
            book.setIsbn(book.getIsbn());
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
        if (requestDto.categoryIds() != null) {
            Set<Category> validCategories = validateCategoryIdsExistence(requestDto.categoryIds());
            book.setCategories(validCategories);
        }
        Book updateBook = bookRepository.save(book);
        return bookMapper.toDto(updateBook);
    }

    @Override
    public void deleteById(Long id) {
        assertBookExistsById(id);
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

    @Override
    public List<BookDtoWithoutCategoryIds> getBooksByCategoryId(Long id, Pageable pageable) {
        return bookRepository.findAllByCategoriesId(id).stream()
                .map(bookMapper::toDtoWithoutCategories)
                .toList();
    }

    private void validateIsbnUniqueness(String isbn, Long id) {
        Optional<Book> bookWithSameIsbn = bookRepository.findByIsbn(isbn);
        if (bookWithSameIsbn.isPresent() && !bookWithSameIsbn.get().getId().equals(id)) {
            throw new EntityAlreadyPresentException("Book with ISBN "
                    + isbn + " is already present");
        }
    }

    private Book assertBookExistsById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find book by id=" + id));
    }

    private Set<Category> validateCategoryIdsExistence(Set<Long> categoryIds) {
        return categoryIds.stream()
                .map(categoryRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }
}
