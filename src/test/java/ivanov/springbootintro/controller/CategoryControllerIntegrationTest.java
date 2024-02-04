package ivanov.springbootintro.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ivanov.springbootintro.dto.book.BookDtoWithoutCategoryIds;
import ivanov.springbootintro.dto.category.CategoryDto;
import ivanov.springbootintro.dto.category.CreateCategoryRequestDto;
import ivanov.springbootintro.model.Category;
import ivanov.springbootintro.repository.category.CategoryRepository;
import java.sql.Connection;
import java.sql.SQLException;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
    private CategoryRepository categoryRepository;

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/categories/remove-all-categories.sql")
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

    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
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
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        CategoryDto[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CategoryDto[].class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(4, actual.length);
        Assertions.assertEquals(1L, actual[0].id());
        Assertions.assertEquals("Fantasy", actual[1].name());
        Assertions.assertEquals("Detectives books", actual[2].description());
        Assertions.assertEquals("Historical", actual[3].name());
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
                    "classpath:database/books/remove-all-books.sql",
                    "classpath:database/books/add-six-default-books.sql",
                    "classpath:database/categories/assign-category-to-book-data.sql"
            }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = {
                    "classpath:database/books/remove-all-books.sql",
                    "classpath:database/categories/remove-all-assign-categories-to-book.sql"
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
        Assertions.assertEquals(2, actual.length);
        Assertions.assertEquals("The Great Gatsby", actual[0].title());
        Assertions.assertEquals("1984", actual[1].title());
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
        Long id = -5L;
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
        Assertions.assertEquals(id, actual.id());
        Assertions.assertEquals("Fiction", actual.name());
        Assertions.assertEquals("Fiction books", actual.description());
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
        Long id = -5L;
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
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/categories")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        Category actualResponse = objectMapper.readValue(result.getResponse()
                .getContentAsString(), Category.class);
        Assertions.assertNotNull(actualResponse);
        Category actual = new Category();
        if (categoryRepository.findById(actualResponse.getId()).isPresent()) {
            actual = categoryRepository.findById(actualResponse.getId()).get();
        }
        Assertions.assertEquals(createCategoryRequestDto.name(), actual.getName());
        Assertions.assertEquals(createCategoryRequestDto.description(), actual.getDescription());
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
        Category actualResponse = objectMapper.readValue(result.getResponse()
                .getContentAsString(), Category.class);
        Assertions.assertNotNull(actualResponse);
        Category actual = new Category();
        if (categoryRepository.findById(actualResponse.getId()).isPresent()) {
            actual = categoryRepository.findById(actualResponse.getId()).get();
        }
        Assertions.assertEquals(createCategoryRequestDto.name(), actual.getName());
        Assertions.assertEquals(createCategoryRequestDto.description(), actual.getDescription());
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
        mockMvc.perform(
                        put("/api/categories/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
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
        Long id = -5L;
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
        // When
        mockMvc.perform(
                        delete("/api/categories/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();
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
        Long id = -5L;
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
