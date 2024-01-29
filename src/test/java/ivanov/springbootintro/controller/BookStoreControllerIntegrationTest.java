package ivanov.springbootintro.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ivanov.springbootintro.dto.book.BookDto;
import ivanov.springbootintro.dto.book.BookSearchParameters;
import ivanov.springbootintro.dto.book.CreateBookRequestDto;
import ivanov.springbootintro.dto.book.UpdateBookRequestDto;
import ivanov.springbootintro.model.Book;
import ivanov.springbootintro.model.Category;
import ivanov.springbootintro.repository.book.BookRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookStoreControllerIntegrationTest {
    private static MockMvc mockMvc;
    private final Set<Long> categoryIds = new HashSet<>();
    private final CreateBookRequestDto createBookRequestDto = new CreateBookRequestDto(
            "Pride and Prejudice",
            "Jane Austen",
            "978-20141439518",
            withBigDecimal(15.50, 2),
            "A classic romantic novel set in early 19th-century England.",
            "pride_prejudice.jpg",
            categoryIds
    );
    private final UpdateBookRequestDto updateBookRequestDto = new UpdateBookRequestDto(
            "Update title",
            "Update author",
            "978-0743273565",
            withBigDecimal(15.50, 2),
            "",
            "",
            categoryIds
    );
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookRepository bookRepository;

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/remove-all-books.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/categories/remove-all-categories.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/categories/remove-all-assign-"
                            + "categories-to-book.sql")
            );
        }
    }

    public BigDecimal withBigDecimal(double value, int places) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(places, RoundingMode.HALF_UP);
        return bigDecimal;
    }

    @BeforeEach
    void beforeEach(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books/add-three-default-books.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/categories/add-four-default-categories.sql")
            );
        }
    }

    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    // FindAllBooks
    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            When method findAllBooks is called by authorised user,
            Then the corresponding list of BookDto should be returned.
            """)
    public void findAllBooks_WithAuthorisedUser_ShouldReturnListBookDto() throws Exception {
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        BookDto[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), BookDto[].class);
        Assertions.assertEquals(3, actual.length);
        Assertions.assertEquals("The Great Gatsby", actual[0].getTitle());
        Assertions.assertEquals("Harper Lee", actual[1].getAuthor());
        Assertions.assertEquals("978-0451524935", actual[2].getIsbn());
    }

    @Test
    @DisplayName("""
            When method findAllBooks is called by not authorised user,
            Then the status Forbidden should be returned.
            """)
    public void findAllBooks_WithNotAuthorisedUser_ShouldReturnStatusForbidden() throws Exception {
        // When
        mockMvc.perform(
                        get("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    // FindBookById
    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            When method findBookById is called by authorised user,
            Then the corresponding BookDto should be returned.
            """)
    public void findBookById_WithValidBookId_ShouldReturnBookDto() throws Exception {
        // Given
        Long id = 1L;
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/books/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        BookDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), BookDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(id, actual.getId());
        Assertions.assertEquals("The Great Gatsby", actual.getTitle());
        Assertions.assertEquals("F. Scott Fitzgerald", actual.getAuthor());
        Assertions.assertEquals("978-0743273565", actual.getIsbn());
        Assertions.assertEquals(BigDecimal.valueOf(12.99), actual.getPrice());
        Assertions.assertEquals("A classic novel about the American Dream.",
                actual.getDescription());
        Assertions.assertEquals("gatsby.jpg", actual.getCoverImage());
    }

    @Test
    @DisplayName("""
            When method findBookById is called by not authorised user,
            Then the status Forbidden should be returned.
            """)
    public void findBookById_WithNotAuthorisedUser_ShouldReturnStatusForbidden() throws Exception {
        // Given
        Long id = 1L;
        // When
        mockMvc.perform(
                        get("/api/books/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            When method findBookById is called by authorised user with invalid book ID,
            Then the status Not Found should be returned.
            """)
    public void findBookById_WithInvalidBookId_ShouldReturnStatusNotFound() throws Exception {
        // Given
        Long id = -5L;
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/books/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertEquals("Can't find book by id=" + id, actual);
    }

    // CreateBook
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            Authorised user with role "ADMIN" given a valid RequestDto,
            When createBook is called,
            Then the corresponding BookDto should be returned.
            """)
    public void createBook_WithValidRequestDto_ShouldReturnValidBookDto() throws Exception {
        // Given
        categoryIds.add(2L);
        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/books")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());

        Book actualBook = new Book();
        if (bookRepository.findByIsbn("978-20141439518").isPresent()) {
            actualBook = bookRepository.findByIsbn("978-20141439518").get();
        }
        Assertions.assertEquals(7, actualBook.getId());
        Assertions.assertEquals(createBookRequestDto.title(), actualBook.getTitle());
        Assertions.assertEquals(createBookRequestDto.author(), actualBook.getAuthor());
        Assertions.assertEquals(createBookRequestDto.isbn(), actualBook.getIsbn());
        Assertions.assertEquals(createBookRequestDto.price(), actualBook.getPrice());
        Assertions.assertEquals(createBookRequestDto.description(), actualBook.getDescription());
        Assertions.assertEquals(createBookRequestDto.coverImage(), actualBook.getCoverImage());
        Set<Long> categoriesId = actualBook.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        Assertions.assertEquals(createBookRequestDto.categoryIds(), categoriesId);
    }

    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            Authorised user without role "ADMIN" given a valid RequestDto,
            When createBook is called,
            Then the status Forbidden should be returned.
            """)
    public void createBook_WithAuthorisedUserWithoutRoleAdmin_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);
        // When
        mockMvc.perform(
                        post("/api/books")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("""
            Non authorised user given a valid RequestDto,
            When createBook is called,
            Then the status Forbidden should be returned.
            """)
    public void createBook_WithNonAuthorisedUser_ShouldReturnStatusForbidden() throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);
        // When
        mockMvc.perform(
                        post("/api/books")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            Authorised user given a invalid RequestDto,
            When createBook is called,
            Then the status BadRequest should be returned.
            """)
    public void createBook_WithInvalidRequestDto_ShouldReturnStatusBadRequest() throws Exception {
        // Given
        categoryIds.add(1L);
        CreateBookRequestDto requestDto = new CreateBookRequestDto(
                "Pride and Prejudice",
                "Jane Austen",
                "",
                BigDecimal.valueOf(15.50),
                "A classic romantic novel set in early 19th-century England.",
                "pride_prejudice.jpg",
                categoryIds
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/books")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        actual = actual.substring(actual.indexOf('[') + 2,
                actual.indexOf(']') - 1);
        Assertions.assertEquals("isbn must not be empty", actual);
    }

    @WithMockUser(username = "user", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            Authorisation user given a valid RequestDto, not unique ISBN
            When createBook is called,
            Then the status Conflict should be returned.
            """)
    public void createBook_WithNotUniqueIsbn_ShouldReturnStatusConflict() throws Exception {
        // Given
        categoryIds.add(1L);
        CreateBookRequestDto requestDto = new CreateBookRequestDto(
                "Pride and Prejudice",
                "Jane Austen",
                "978-0743273565",
                BigDecimal.valueOf(15.50),
                "A classic romantic novel set in early 19th-century England.",
                "pride_prejudice.jpg",
                categoryIds
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/books")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict())
                .andReturn();
        // Then
        Assertions.assertEquals("Can't create a book."
                        + " Book with Isbn " + requestDto.isbn() + " is already present",
                result.getResponse().getContentAsString());
    }

    // UpdateBookById
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            Authorised user with role "ADMIN" given a valid RequestDto,
            When updateBookById is called,
            Then the corresponding BookDto should be returned.
            """)
    public void updateBookById_WithValidRequestDto_ShouldReturnValidBookDto() throws Exception {
        // Given
        categoryIds.add(1L);
        categoryIds.add(2L);
        String jsonRequest = objectMapper.writeValueAsString(updateBookRequestDto);
        Long id = 1L;
        // When
        MvcResult result = mockMvc.perform(
                        put("/api/books/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());

        Book actualBook = new Book();
        if (bookRepository.findByIsbn(updateBookRequestDto.isbn()).isPresent()) {
            actualBook = bookRepository.findByIsbn(updateBookRequestDto.isbn()).get();
        }
        Assertions.assertEquals(1L, actualBook.getId());
        Assertions.assertEquals(updateBookRequestDto.title(), actualBook.getTitle());
        Assertions.assertEquals(updateBookRequestDto.author(), actualBook.getAuthor());
        Assertions.assertEquals(updateBookRequestDto.isbn(), actualBook.getIsbn());
        Assertions.assertEquals(updateBookRequestDto.price(), actualBook.getPrice());
        Assertions.assertEquals(updateBookRequestDto.description(), actualBook.getDescription());
        Assertions.assertEquals(updateBookRequestDto.coverImage(), actualBook.getCoverImage());
        Set<Long> categoriesId = actualBook.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        Assertions.assertEquals(updateBookRequestDto.categoryIds(), categoriesId);
    }

    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            Authorised user without role "ADMIN" given a valid RequestDto,
            When updateBookById is called,
            Then the status Forbidden should be returned.
            """)
    public void updateBookById_WithAuthorisedUserWithoutRoleAdmin_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(updateBookRequestDto);
        Long id = 1L;
        // When
        mockMvc.perform(
                        put("/api/books/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("""
            Non authorised user given a valid RequestDto,
            When updateBookById is called,
            Then the status Forbidden should be returned.
            """)
    public void updateBookById_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(updateBookRequestDto);
        Long id = 1L;
        // When
        mockMvc.perform(
                        put("/api/books/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @WithMockUser(username = "user", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            Authorisation user given a valid RequestDto, not unique ISBN
            When updateBookById is called,
            Then the status Conflict should be returned.
            """)
    public void updateBookById_WithNotUniqueIsbn_ShouldReturnStatusConflict() throws Exception {
        // Given
        categoryIds.add(1L);
        UpdateBookRequestDto requestDto = new UpdateBookRequestDto(
                "Update title",
                "Update author",
                "978-0061120084",
                BigDecimal.valueOf(15.50),
                "",
                "",
                categoryIds
        );
        Long id = 1L;
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        // When
        MvcResult result = mockMvc.perform(
                        put("/api/books/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict())
                .andReturn();
        // Then
        Assertions.assertEquals("Book with ISBN " + requestDto.isbn() + " is already present",
                result.getResponse().getContentAsString());
    }

    @WithMockUser(username = "user", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            When method updateBookById is called with invalid book ID,
            Then the status Not Found should be returned.
            """)
    public void updateBookById_WithInvalidBookId_ShouldReturnStatusNotFound() throws Exception {
        // Given
        categoryIds.add(1L);
        UpdateBookRequestDto requestDto = new UpdateBookRequestDto(
                "Update title",
                "Update author",
                "978-1061120084",
                BigDecimal.valueOf(15.50),
                "",
                "",
                categoryIds
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        Long id = -5L;
        // When
        MvcResult result = mockMvc.perform(
                        put("/api/books/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertEquals("Can't find book by id=" + id, actual);
    }

    // DeleteBookById
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            Authorised user with role "ADMIN" given a valid RequestDto,
            When deleteBookById is called,
            Then the status No Content should be returned.
            Book by id delete.
            """)
    public void deleteBookById_WithValidRequestDto_SShouldReturnStatusNoContent() throws Exception {
        // Given
        Long id = 1L;
        // When
        mockMvc.perform(
                        delete("/api/books/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            Authorised user without role "ADMIN" given a valid RequestDto,
            When deleteBookById is called,
            Then the status Forbidden should be returned.
            """)
    public void deleteBookById_WithAuthorisedUserWithoutRoleAdmin_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long id = 1L;
        // When
        mockMvc.perform(
                        delete("/api/books/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("""
            Non authorised user given a valid RequestDto,
            When deleteBookById is called,
            Then the status Forbidden should be returned.
            """)
    public void deleteBookById_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long id = 1L;
        // When
        mockMvc.perform(
                        delete("/api/books/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @WithMockUser(username = "user", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            When method deleteBookById is called with invalid book ID,
            Then the status Not Found should be returned.
            """)
    public void deleteBookById_WithInvalidBookId_ShouldReturnStatusNotFound() throws Exception {
        // Given
        Long id = -5L;
        // When
        MvcResult result = mockMvc.perform(
                        delete("/api/books/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertEquals("Can't find book by id=" + id, actual);
    }

    // Search
    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            Book with search parameters present,
            When method search is called by authorised user,
            Then the corresponding list BookDto should be returned.
            """)
    public void searchBook_WithPresentParameters_ShouldReturnListBookDto() throws Exception {
        // Given
        BookSearchParameters bookSearchParameters = new BookSearchParameters(
                new String[]{"1984", "The Great Gatsby"},
                new String[]{"George Orwell", "F. Scott Fitzgerald"}
        );
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/books/search")
                                .param("titles", bookSearchParameters.titles())
                                .param("authors", bookSearchParameters.authors())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        BookDto[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), BookDto[].class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.length);
        Assertions.assertEquals(1, actual[0].getId());
        Assertions.assertEquals(3, actual[1].getId());
        Assertions.assertEquals("The Great Gatsby", actual[0].getTitle());
        Assertions.assertEquals("1984", actual[1].getTitle());
        Assertions.assertEquals("F. Scott Fitzgerald", actual[0].getAuthor());
        Assertions.assertEquals("George Orwell", actual[1].getAuthor());
    }

    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            Book with search parameters not present,
            When method search is called by authorised user,
            Then the empty list should be returned.
            """)
    public void searchBook_WithNotPresentParameters_ShouldReturnEmptyList() throws Exception {
        // Given
        BookSearchParameters bookSearchParameters = new BookSearchParameters(
                new String[]{"1984 years"},
                new String[]{"mr. George Orwell"}
        );
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/books/search")
                                .param("titles", bookSearchParameters.titles())
                                .param("authors", bookSearchParameters.authors())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        BookDto[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), BookDto[].class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(0, actual.length);
    }

    @Test
    @DisplayName("""
            When method search is called by not authorised user,
            Then the status Forbidden should be returned.
            """)
    public void searchBook_WithNotAuthorisedUser_ShouldReturnStatusForbidden() throws Exception {
        // When
        mockMvc.perform(
                        get("/api/books/search")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }
}
