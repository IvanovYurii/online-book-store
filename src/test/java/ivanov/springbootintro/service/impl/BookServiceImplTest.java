package ivanov.springbootintro.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ivanov.springbootintro.dto.book.BookDto;
import ivanov.springbootintro.dto.book.BookDtoWithoutCategoryIds;
import ivanov.springbootintro.dto.book.CreateBookRequestDto;
import ivanov.springbootintro.dto.book.UpdateBookRequestDto;
import ivanov.springbootintro.exception.EntityAlreadyPresentException;
import ivanov.springbootintro.exception.EntityNotFoundException;
import ivanov.springbootintro.mapper.impl.BookMapperImpl;
import ivanov.springbootintro.model.Book;
import ivanov.springbootintro.model.Category;
import ivanov.springbootintro.repository.book.BookRepository;
import ivanov.springbootintro.repository.category.CategoryRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    private final Set<Long> categoryIds = new HashSet<>();
    private final Set<Category> categories = createCategories();
    private final BigDecimal price = BigDecimal.valueOf(15.50);

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapperImpl bookMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookDto bookDto;
    private CreateBookRequestDto createBookRequestDto;
    private UpdateBookRequestDto updateBookRequestDto;

    @BeforeEach
    void setUp() {
        categoryIds.add(1L);
        categoryIds.add(2L);

        book = new Book();
        book.setId(1L);
        book.setTitle("Kobzar");
        book.setAuthor("Shevchenko");
        book.setIsbn("123456789");
        book.setPrice(BigDecimal.valueOf(15.50));
        book.setDescription("Literary");
        book.setCoverImage("https://www.org.net");
        book.setCategories(categories);

        createBookRequestDto = new CreateBookRequestDto(
                "Kobzar",
                "Shevchenko",
                "123456789",
                price,
                "Literary",
                "https://www.org.net",
                categoryIds
        );

        updateBookRequestDto = new UpdateBookRequestDto(
                "Kobzar",
                "Alice",
                "123456789",
                BigDecimal.valueOf(-9.5),
                "",
                "https://www.org.net",
                categoryIds
        );

        bookDto = new BookDto();
        bookDto.setId(1L);
        bookDto.setTitle("Kobzar");
        bookDto.setAuthor("Shevchenko");
        bookDto.setIsbn("123456789");
        bookDto.setPrice(price);
        bookDto.setDescription("Literary");
        bookDto.setCoverImage("https://www.org.net");
        bookDto.setCategoryIds(categoryIds);
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    // Create
    @Test
    @DisplayName("""
            Given a valid RequestDto,
            When create is called,
            Then the corresponding BookDto should be returned.
            """)
    public void createBook_WithValidRequestDto_ShouldReturnBookDto() {
        // Given
        when(bookMapper.toEntity(createBookRequestDto)).thenReturn(book);
        when(bookRepository.findByIsbn(createBookRequestDto.isbn())).thenReturn(Optional.empty());
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        // When
        BookDto actualBook = bookService.create(createBookRequestDto);
        // Then
        assertThat(actualBook).isEqualTo(bookDto);
        EqualsBuilder.reflectionEquals(bookDto, actualBook);
        verify(bookRepository, times(1)).findByIsbn(book.getIsbn());
        verify(bookRepository, times(1)).save(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Given not unique ISBN in CreateBookRequestDto,
            When create is called,
            Then throw EntityAlreadyPresentException should be returned.
            """)
    public void
            createBook_WithNotUniqueIsbnInRequestDto_ShouldThrowEntityAlreadyPresentException() {
        // Given
        when(bookRepository.findByIsbn(book.getIsbn()))
                .thenReturn(Optional.of(book));
        // When
        Exception actual = assertThrows(
                EntityAlreadyPresentException.class,
                () -> bookService.create(createBookRequestDto)
        );
        // Verify
        String expected = "Can't create a book. Book with Isbn " + book.getIsbn()
                + " is already present";
        assertEquals(expected, actual.getMessage());
        verify(bookRepository, times(1)).findByIsbn(book.getIsbn());
        verify(bookRepository, never()).save(book);
        verifyNoMoreInteractions(bookRepository);
    }

    // FindAll
    @Test
    @DisplayName("""
            Given a list of existing books in the database,
            When the findAll method is called with pagination,
            Then the corresponding list of BookDto should be returned.
            """)
    public void findAllBooks_ExistingData_ShouldReturnListBookDto() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        // When
        List<BookDto> actualListBook = bookService.findAll(pageable);
        // Then
        assertThat(actualListBook).hasSize(1);
        assertEquals(actualListBook.get(0), bookDto);
        verify(bookRepository, times(1)).findAll(pageable);
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            When findAll is called with no existing data,
            Then an empty list should be returned.
            """)
    public void findAllBooks_NoExistingData_ShouldReturnEmptyList() {
        // Given
        when(bookRepository.findAll(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        // When
        List<BookDto> actualListBook = bookService.findAll(Pageable.unpaged());
        // Then
        assertTrue(actualListBook.isEmpty());
        verify(bookRepository, times(1)).findAll(Pageable.unpaged());
        verifyNoMoreInteractions(bookRepository);
    }

    // FindById
    @Test
    @DisplayName("""
            Given a valid book ID,
            When findById is called,
            Then the corresponding BookDto should be returned.
            """)
    public void findBookById_WithValidBookId_ShouldReturnBookDto() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        // When
        BookDto actualBook = bookService.findById(1L);
        // Then
        assertThat(actualBook).isEqualTo(bookDto);
        EqualsBuilder.reflectionEquals(bookDto, actualBook);
        verify(bookRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Given an invalid book ID,
            When findById is called,
            Then throw EntityNotFoundException should be thrown.
            """)
    public void findBookById_WithInvalidBookId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidBookId = 999L;
        when(bookRepository.findById(invalidBookId)).thenReturn(Optional.empty());
        // When
        Exception actual = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.findById(invalidBookId));
        // Then
        String expected = "Can't find book by id=" + invalidBookId;
        assertEquals(expected, actual.getMessage());
        verify(bookRepository, times(1)).findById(invalidBookId);
        verifyNoMoreInteractions(bookRepository);
    }

    // UpdateById
    @Test
    @DisplayName("""
            Given a valid RequestDto,
            When updateById is called,
            Then the corresponding BookDto should be returned.
            """)
    public void updateBookById_WithValidRequestDto_ShouldReturnBookDto() {
        // Given
        bookDto.setAuthor(updateBookRequestDto.author());
        bookDto.setPrice(
                updateBookRequestDto.price() != null
                        ? updateBookRequestDto.price().max(BigDecimal.ZERO) :
                        book.getPrice()
        );
        bookDto.setDescription(updateBookRequestDto.description());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        // When
        BookDto actualBook = bookService.updateById(updateBookRequestDto, 1L);
        // Then
        assertThat(actualBook).isEqualTo(bookDto);
        EqualsBuilder.reflectionEquals(book, actualBook);
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    @DisplayName("""
            Given a invalid book ID,
            When updateById is called,
            Then throw EntityNotFoundException should be thrown.
            """)
    public void updateBookById_WithInvalidRequestDto_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidBookId = 999L;
        when(bookRepository.findById(invalidBookId)).thenReturn(Optional.empty());
        // When
        Exception actual = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.updateById(updateBookRequestDto, invalidBookId));
        // Then
        String expected = "Can't find book by id=" + invalidBookId;
        assertEquals(expected, actual.getMessage());
        verify(bookRepository, times(1)).findById(999L);
        verify(bookRepository, never()).save(any());
        verifyNoMoreInteractions(bookRepository);

    }

    @Test
    @DisplayName("""
            Given not unique ISBN in RequestDto,
            When update is called,
            Then throw EntityAlreadyPresentException should be returned.
            """)
    public void updateBookById_WithNotUniqueIsbnIn_ShouldThrowEntityAlreadyPresentException(
    ) {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        book.setId(2L);
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.of(book));

        // When and Then
        EntityAlreadyPresentException actual = assertThrows(
                EntityAlreadyPresentException.class,
                () -> bookService.updateById(updateBookRequestDto, 1L)
        );
        // Verify
        String expected = "Book with ISBN " + updateBookRequestDto.isbn() + " is already present";
        assertEquals(expected, actual.getMessage());
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).findByIsbn(book.getIsbn());
        verify(bookRepository, never()).save(book);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("""
            Given a valid ISBN book, not empty, null, or empty other fields, valid and invalid
            categories IDs
            When update is called
            Then the corresponding BookDto should be returned.
            """)
    public void updateBookById_WithOnlyValidIsbn_ShouldReturnBookDto() {
        // Given
        UpdateBookRequestDto requestDto = new UpdateBookRequestDto(
                null,
                "Alice",
                "123456789",
                BigDecimal.valueOf(-1.50),
                "",
                "no  image",
                categoryIds
        );

        book.setAuthor(requestDto.author());
        book.setPrice(
                requestDto.price() != null
                        ? requestDto.price().max(BigDecimal.ZERO) :
                        book.getPrice()
        );
        book.setDescription(requestDto.description());

        bookDto.setAuthor(book.getAuthor());
        bookDto.setPrice(book.getPrice());
        bookDto.setDescription(book.getDescription());

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);
        //when(categoryRepository.findById(1L)).thenReturn(bookDto); мокати категорії по айді
        // When
        BookDto actualBook = bookService.updateById(requestDto, 1L);
        // Then
        assertThat(bookDto).isEqualTo(actualBook);
        verify(bookRepository, times(1)).findById(1L);
        verify(bookMapper, times(1)).toDto(book);
        verify(bookRepository, times(1)).save(book);
    }


    // DeleteById
    @Test
    @DisplayName("""
            Given valid book ID,
            When deleteById is called,
            Then the book should be deleted
            """)
    public void deleteBookById_WithValidBookId_ShouldDeleteBook() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(new Book()));
        // When
        bookService.deleteById(1L);
        // Then
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).deleteById(1L);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("""
            Given invalid book ID,
            When deleteById is called,
            Then throw EntityNotFoundException should be thrown
            """)
    public void deleteBookById_WithInvalidBookId_ShouldThrowEntityNotFoundException() {
        // Given
        Long invalidBookId = 999L;
        when(bookRepository.findById(invalidBookId)).thenReturn(Optional.empty());
        // When
        Exception actual = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.findById(invalidBookId));
        // Then
        String expected = "Can't find book by id=" + invalidBookId;
        assertEquals(expected, actual.getMessage());
        verify(bookRepository, times(1)).findById(999L);
        verify(bookRepository, never()).deleteById(any());
        verifyNoMoreInteractions(bookRepository);
    }

    // GetBooksByCategoryId
    @Test
    @DisplayName("""
            Given a list of existing books in the database,
            When the getBooksByCategoryId method is called with pagination,
            Then the corresponding list of BookDtoWithoutCategories should be returned.
            """)
    public void getAllBooksByCategoryId_ExistingData_ShouldReturnListBookDtoWithoutCategories() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        BookDtoWithoutCategoryIds bookDtoWithoutCategoryIds = new BookDtoWithoutCategoryIds(
                1L,
                "Kobzar",
                "Shevchenko",
                "123456789",
                BigDecimal.valueOf(15.50),
                "Literary",
                "https://www.org.net"
        );
        List<Book> books = List.of(book);
        when(bookRepository.findAllByCategoriesId(1L)).thenReturn(books);
        when(bookMapper.toDtoWithoutCategories(any(Book.class)))
                .thenReturn(bookDtoWithoutCategoryIds);
        // When
        List<BookDtoWithoutCategoryIds> actualListBook = bookService
                .getBooksByCategoryId(1L, pageable);
        // Then
        assertThat(actualListBook).hasSize(1);
        assertEquals(actualListBook.get(0), bookDtoWithoutCategoryIds);
        verify(bookRepository, times(1)).findAllByCategoriesId(1L);
        verify(bookMapper, times(1)).toDtoWithoutCategories(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            When getBooksByCategoryId is called with no existing data,
            Then an empty list should be returned.
            """)
    public void getAllBooksByCategoryId_NoExistingData_ShouldReturnEmptyList() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findAllByCategoriesId(1L)).thenReturn(new ArrayList<>());
        // When
        List<BookDtoWithoutCategoryIds> actualListBooks = bookService
                .getBooksByCategoryId(1L, pageable);
        // Then
        assertTrue(actualListBooks.isEmpty());
        verify(bookRepository, times(1)).findAllByCategoriesId(1L);
        verifyNoMoreInteractions(bookRepository);
    }

    private Set<Category> createCategories() {
        final Set<Category> categories = new HashSet<>();
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("CategoryName1");
        category1.setDescription("CategoryDescription1");
        categories.add(category1);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("CategoryName2");
        category2.setDescription("CategoryDescription2");
        categories.add(category2);
        return categories;
    }
}
