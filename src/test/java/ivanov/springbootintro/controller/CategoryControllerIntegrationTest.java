package ivanov.springbootintro.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ivanov.springbootintro.dto.book.BookDtoWithoutCategoryIds;
import ivanov.springbootintro.dto.category.CategoryDto;
import ivanov.springbootintro.dto.category.CreateCategoryRequestDto;
import ivanov.springbootintro.mapper.BookMapper;
import ivanov.springbootintro.mapper.CategoryMapper;
import ivanov.springbootintro.model.Category;
import ivanov.springbootintro.repository.book.BookRepository;
import ivanov.springbootintro.repository.category.CategoryRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerIntegrationTest {
    private static MockMvc mockMvc;
    private final CreateCategoryRequestDto createCategoryRequestDto = new CreateCategoryRequestDto(
            "Scientific",
            "Scientific books"
    );

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryMapper categoryMapper;

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/remove-all-data.sql")
            );
        }
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
                    new ClassPathResource("database/categories/add-four-default-categories.sql")
            );
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    // GetAllCategories
    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            When method getAllCategories is called by authorised user,
            Then the corresponding list of CategoryDto should be returned.
            """)
    public void getAllCategories_WithAuthorisedUser_ShouldReturnListCategoryDto()
            throws Exception {
        // Given
        List<CategoryDto> expected = categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .toList();
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        List<CategoryDto> actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), new TypeReference<>() {
                });
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertTrue(expected.containsAll(actual));
    }

    @Test
    @DisplayName("""
            When method getAllCategories is called by not authorised user,
            Then the status Forbidden should be returned.
            """)
    public void getAllCategories_WithNotAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // When
        mockMvc.perform(
                        get("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    // GetBooksByCategoryId
    @WithMockUser(username = "admin")
    @Test
    @Sql(
            scripts = {
                    "classpath:database/remove-all-data.sql",
                    "classpath:database/books/add-six-default-books.sql",
                    "classpath:database/categories/add-four-default-categories.sql",
                    "classpath:database/categories/assign-category-to-book-data.sql"
            }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = {
                    "classpath:database/remove-all-data.sql",
            }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("""
            When method getBooksByCategoryId is called by authorised user,
            Books with CategoryId present,
            Then the corresponding BookDtoWithoutCategoryIds should be returned.
            """)
    public void getBooksByCategoryId_BooksPresentWithCategoryId_ShouldReturnListBooks()
            throws Exception {
        // Given
        Long id = 1L;
        List<BookDtoWithoutCategoryIds> expected = bookRepository.findAllByCategoriesId(id).stream()
                .map(bookMapper::toDtoWithoutCategories)
                .toList();
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/categories/{id}/books", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        List<BookDtoWithoutCategoryIds> actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), new TypeReference<>() {
                });
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertTrue(expected.containsAll(actual));
    }

    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            When method getBooksByCategoryId is called by authorised user,
            Books with CategoryId not present,
            Then the empty list should be returned.
            """)
    public void getBooksByCategoryId_BooksNotPresentWithCategoryId_ShouldReturnEmptyList()
            throws Exception {
        // Given
        Long id = 1L;
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/categories/{id}/books", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        BookDtoWithoutCategoryIds[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), BookDtoWithoutCategoryIds[].class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(0, actual.length);
    }

    @Test
    @DisplayName("""
            When method getCategoryById is called by not authorised user,
            Then the status Forbidden should be returned.
            """)
    public void getBooksByCategoryId_WithNotAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long id = 1L;
        // When
        mockMvc.perform(
                        get("/api/categories/{id}/books", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            When method getCategoryById is called by authorised user with invalid Category ID,
            Then the empty list should be returned.
            """)
    public void getBooksByCategoryId_WithInvalidCategoryId_ShouldReturnEmptyList()
            throws Exception {
        // Given
        Long id = 999L;
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/categories/{id}/books", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        BookDtoWithoutCategoryIds[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), BookDtoWithoutCategoryIds[].class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(0, actual.length);
    }

    // GetCategoryById
    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            When method getCategoryById is called by authorised user,
            Then the corresponding CategoryDto should be returned.
            """)
    public void getCategoryById_WithValidCategoryId_ShouldReturnCategoryDto()
            throws Exception {
        // Given
        Long id = 1L;
        CategoryDto expected = categoryMapper.toDto(categoryRepository.findById(id).orElseThrow());
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/categories/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        CategoryDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CategoryDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(expected, actual));
    }

    @Test
    @DisplayName("""
            When method getCategoryById is called by not authorised user,
            Then the status Forbidden should be returned.
            """)
    public void getCategoryById_WithNotAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long id = 1L;
        // When
        mockMvc.perform(
                        get("/api/categories/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            When method getCategoryById is called by authorised user with invalid Category ID,
            Then the status Not Found should be returned.
            """)
    public void getCategoryById_WithInvalidCategoryId_ShouldReturnStatusNotFound()
            throws Exception {
        // Given
        Long id = 999L;
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/categories/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertEquals("Can't find category by id=" + id, actual);
    }

    // CreateCategory
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            Authorised user with role "ADMIN" given a valid RequestDto,
            When createCategory is called,
            Then the corresponding CategoryDto should be returned.
            """)
    public void createCategory_WithValidRequestDto_ShouldReturnValidCategoryDto()
            throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(createCategoryRequestDto);
        CategoryDto expected = categoryMapper.toDto(categoryMapper
                .toEntity(createCategoryRequestDto));
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/categories")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        CategoryDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CategoryDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(expected, actual,
                "id"));
    }

    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            Authorised user without role "ADMIN" given a valid RequestDto,
            When createCategory is called,
            Then the status Forbidden should be returned.
            """)
    public void createCategory_WithAuthorisedUserWithoutRoleAdmin_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(createCategoryRequestDto);
        // When
        mockMvc.perform(
                        post("/api/categories")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("""
            Non authorised user given a valid RequestDto,
            When createCategory is called,
            Then the status Forbidden should be returned.
            """)
    public void createCategory_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(createCategoryRequestDto);
        // When
        mockMvc.perform(
                        post("/api/categories")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            Authorised user with role "ADMIN" given a invalid RequestDto,
            When createCategory is called,
            Then the status BadRequest should be returned.
            """)
    public void createCategory_WithInvalidRequestDto_ShouldReturnStatusBadRequest()
            throws Exception {
        // Given
        String jsonRequest = "";
        // When
        mockMvc.perform(
                        post("/api/categories")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    // UpdateCategory
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            Authorised user with role "ADMIN" given a valid RequestDto,
            When updateCategory is called,
            Then the corresponding CategoryDto should be returned.
            """)
    public void updateCategoryById_WithValidRequestDto_ShouldReturnValidCategoryDto()
            throws Exception {
        // Given
        Long id = 1L;
        String jsonRequest = objectMapper.writeValueAsString(createCategoryRequestDto);
        // When
        MvcResult result = mockMvc.perform(
                        put("/api/categories/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        CategoryDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CategoryDto.class);
        Assertions.assertNotNull(actual);
        CategoryDto updatedCategory = categoryMapper.toDto(categoryRepository
                .findById(id).orElseThrow());
        Assertions.assertTrue(EqualsBuilder.reflectionEquals(updatedCategory, actual));
    }

    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            Authorised user without role "ADMIN" given a valid RequestDto,
            When updateCategory is called,
            Then the status Forbidden should be returned.
            """)
    public void updateCategoryById_WithAuthorisedUserWithoutRoleAdmin_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long id = 1L;
        String jsonRequest = objectMapper.writeValueAsString(createCategoryRequestDto);
        // When
        mockMvc.perform(
                        put("/api/categories/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("""
            Non authorised user given a valid RequestDto,
            When updateCategory is called,
            Then the status Forbidden should be returned.
            """)
    public void updateCategoryById_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long id = 1L;
        String jsonRequest = objectMapper.writeValueAsString(createCategoryRequestDto);
        // When
        mockMvc.perform(
                        put("/api/categories/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            Authorised user with role "ADMIN" given a invalid RequestDto,
            When updateCategory is called,
            Then the status BadRequest should be returned.
            """)
    public void updateCategoryById_WithInvalidRequestDto_ShouldReturnStatusBadRequest()
            throws Exception {
        // Given
        Long id = 1L;
        String jsonRequest = "";
        // When
        MvcResult result = mockMvc.perform(
                        put("/api/categories/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        Assertions.assertTrue(result.getResponse().getContentAsString()
                .contains("Failed to read request"));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            When method updateCategory is called by authorised user with invalid Category ID,
            Then the status Not Found should be returned.
            """)
    public void updateCategoryById_WithInvalidCategoryId_ShouldReturnStatusNotFound()
            throws Exception {
        // Given
        Long id = 999L;
        String jsonRequest = objectMapper.writeValueAsString(createCategoryRequestDto);
        // When
        MvcResult result = mockMvc.perform(
                        put("/api/categories/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertEquals("Can't find category by id=" + id, actual);
    }

    // DeleteCategory
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            Authorised user with role "ADMIN" given a valid RequestDto,
            When deleteCategory is called,
            Then the status No Content should be returned,
            Category by id delete.
            """)
    public void deleteCategoryById_WithValidRequestDto_ShouldReturnStatusNoContent()
            throws Exception {
        // Given
        Long id = 1L;
        List<Category> expected = categoryRepository.findAll();
        // When
        mockMvc.perform(
                        delete("/api/categories/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();
        // Then
        List<Category> actual = categoryRepository.findAll();
        Assertions.assertEquals(expected.size() - 1, actual.size());
        Category deletedCategory = categoryRepository.findById(id).orElse(null);
        Assertions.assertNull(deletedCategory);
    }

    @WithMockUser(username = "admin")
    @Test
    @DisplayName("""
            Authorised user without role "ADMIN" given a valid RequestDto,
            When deleteCategory is called,
            Then the status Forbidden should be returned.
            """)
    public void deleteCategoryById_WithAuthorisedUserWithoutRoleAdmin_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long id = 1L;
        // When
        mockMvc.perform(
                        delete("/api/categories/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("""
            Non authorised user given a valid RequestDto,
            When deleteCategory is called,
            Then the status Forbidden should be returned.
            """)
    public void deleteCategoryById_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long id = 1L;
        // When
        mockMvc.perform(
                        delete("/api/categories/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("""
            When method deleteCategory is called by authorised user with invalid Category ID,
            Then the status Not Found should be returned.
            """)
    public void deleteCategoryById_WithInvalidCategoryId_ShouldReturnStatusNotFound()
            throws Exception {
        // Given
        Long id = 999L;
        // When
        MvcResult result = mockMvc.perform(
                        delete("/api/categories/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertEquals("Can't find category by id=" + id, actual);
    }
}
