package ivanov.springbootintro.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ivanov.springbootintro.dto.cartitem.AddCartItemRequestDto;
import ivanov.springbootintro.dto.cartitem.CartItemDto;
import ivanov.springbootintro.dto.cartitem.UpdateCartItemQuantityBookRequestDto;
import ivanov.springbootintro.dto.shoppingcart.ShoppingCartDto;
import ivanov.springbootintro.mapper.CartItemMapper;
import ivanov.springbootintro.model.CartItem;
import ivanov.springbootintro.model.ShoppingCart;
import ivanov.springbootintro.repository.shoppingcart.ShoppingCartRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerIntegrationTest {
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private CartItemMapper cartItemMapper;

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

    @BeforeAll
    static void beforeAll(
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
                    new ClassPathResource("database/books/add-six-default-books.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/categories/add-four-default-categories.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/categories/assign-category-to-book-data.sql")
            );
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @BeforeEach
    void setUp(
            @Autowired DataSource dataSource
    ) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/cart_items/remove-all-cart_items.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/cart_items/add-default-cart-items.sql")
            );
        }
    }

    @AfterEach
    void tearDown(
            @Autowired DataSource dataSource
    ) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/cart_items/remove-all-cart_items.sql")
            );
        }
    }

    // Get User Shopping Cart
    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method getUserShoppingCart is called with authorised existing user,
            Then the corresponding ShoppingCartDto should be returned.
            """)
    public void getUserShoppingCart_WithExistingUser_ShouldReturnShoppingCartDto()
            throws Exception {
        // Given
        ShoppingCart expectedShoppingCart = shoppingCartRepository
                .getShoppingCartByUserEmail("bob.jones@example.com");
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        ShoppingCartDto actualCard = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ShoppingCartDto.class);
        List<CartItemDto> actual = actualCard.cartItems().stream().toList();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedShoppingCart.getCartItems().size(), actual.size());
        //Чи потрібно весь список порівнювати і як? Достатньо порівняти кількість у списках?
        // Достатньо відфільтпувати одну книгу і хардкордом звірити поля?
        Optional<CartItemDto> matchingItem = actual.stream()
                .filter(item -> "The Great Gatsby".equals(item.bookTitle()))
                .findFirst();
        Assertions.assertTrue(matchingItem.isPresent());
        CartItemDto matchingCartItem = matchingItem.get();
        Assertions.assertEquals("The Great Gatsby", matchingCartItem.bookTitle());
        Assertions.assertEquals(3, matchingCartItem.quantity());
    }

    @Test
    @DisplayName("""
            When method getUserShoppingCart is called with non authorised user,
            Then the status Forbidden should be returned.
            """)
    public void getUserShoppingCart_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // When
        mockMvc.perform(
                        get("/api/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    // Add Item To Shopping Cart
    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method addItemToShoppingCart is called by authorised existing user,
            Item is present in data base and not present in shopping cart
            Then the corresponding ShoppingCartDto should be returned.
            """)
    public void addItemToShoppingCart_WithItemNotPresentInShoppCart_ShouldReturnShoppingCartDto()
            throws Exception {
        // Given
        AddCartItemRequestDto addCartItemRequestDto =
                new AddCartItemRequestDto(2L, 3);
        String jsonRequest = objectMapper.writeValueAsString(addCartItemRequestDto);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/cart")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        CartItemDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CartItemDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(addCartItemRequestDto.bookId(), actual.bookId());
        Assertions.assertEquals(addCartItemRequestDto.quantity(), actual.quantity());
    }

    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method addItemToShoppingCart is called by authorised existing user,
            Item is present in data base and present in shopping cart
            Then the corresponding ShoppingCartDto should be returned with update quantity.
            """)
    public void addItemToShoppingCart_WithItemPresentInShoppingCart_ShouldReturnShoppingCartDto()
            throws Exception {
        // Given
        AddCartItemRequestDto addCartItemRequestDto =
                new AddCartItemRequestDto(1L, 3);
        String jsonRequest = objectMapper.writeValueAsString(addCartItemRequestDto);
        ShoppingCart exceptedCard = shoppingCartRepository
                .getShoppingCartByUserEmail("bob.jones@example.com");
        List<CartItem> actualItems = exceptedCard.getCartItems().stream().toList();
        Optional<CartItem> expectedCartItem = actualItems.stream()
                .filter(item -> addCartItemRequestDto.bookId().equals(item.getBook().getId()))
                .findFirst();
        CartItemDto expectedDto = expectedCartItem
                .map(cartItemMapper::toDto)
                .orElse(null);
        assert expectedDto != null;
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/cart")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        CartItemDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CartItemDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedDto.bookId(), actual.bookId());
        Assertions.assertEquals(expectedDto.bookTitle(), actual.bookTitle());
        Assertions.assertEquals(expectedDto.quantity() + addCartItemRequestDto.quantity(),
                actual.quantity());
    }

    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method addItemToShoppingCart is called with authorised existing user,
            Item Not present in data base
            Then the status NOT FOUND should be returned.
            """)
    public void addItemToShoppingCart_WithItemNotPresentInDB_ShouldReturnStatusNotFound()
            throws Exception {
        // Given
        AddCartItemRequestDto addCartItemRequestDto =
                new AddCartItemRequestDto(999L, 3);
        String jsonRequest = objectMapper.writeValueAsString(addCartItemRequestDto);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/cart")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        System.out.println(actual);
        Assertions.assertEquals("Can't find book by id=" + addCartItemRequestDto.bookId(),
                actual);
    }

    @Test
    @DisplayName("""
            When method addItemToShoppingCart is called with non authorised user,
            Then the status Forbidden should be returned.
            """)
    public void addItemToShoppingCart_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();

        String actual = result.getResponse().getContentAsString();
        System.out.println(actual);
    }

    // Update Cart Item Quantity
    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method updateCartItemQuantity is called by authorised existing user,
            Item is present in data base and present in shopping cart
            Then the corresponding ShoppingCartDto should be returned with update quantity.
            """)
    public void updateCartItemQuantity_WithItemPresentInShoppingCart_ShouldReturnShoppingCartDto()
            throws Exception {
        // Given
        Long cartItemId = 4L;
        ShoppingCart expectedCart = shoppingCartRepository
                .getShoppingCartByUserEmail("bob.jones@example.com");
        List<CartItem> actualItems = expectedCart.getCartItems().stream().toList();
        Optional<CartItem> expectedOptional = actualItems.stream()
                .filter(item -> cartItemId.equals(item.getId()))
                .findFirst();
        CartItemDto expectedDto = expectedOptional
                .map(cartItemMapper::toDto)
                .orElse(null);
        assert expectedDto != null;
        UpdateCartItemQuantityBookRequestDto updateCartItemQuantity =
                new UpdateCartItemQuantityBookRequestDto(5);
        String jsonRequest = objectMapper.writeValueAsString(updateCartItemQuantity);
        // When
        MvcResult result = mockMvc.perform(
                        put("/api/cart/cart-items/{cartItemId}", cartItemId)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        CartItemDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), CartItemDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedDto.bookId(), actual.bookId());
        Assertions.assertEquals(expectedDto.bookTitle(), actual.bookTitle());
        Assertions.assertEquals(updateCartItemQuantity.quantity(), actual.quantity());
    }

    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method updateCartItemQuantity is called by authorised existing user,
            Item is present in data base and not present in shopping cart
            Then the status NOT FOUND should be returned.
            """)
    public void updateCartItemQuantity_WithItemNotPresentInShoppCart_ShouldReturnStatusNotFound()
            throws Exception {
        // Given
        Long cartItemId = 1L;
        UpdateCartItemQuantityBookRequestDto updateCartItemQuantity =
                new UpdateCartItemQuantityBookRequestDto(5);
        String jsonRequest = objectMapper.writeValueAsString(updateCartItemQuantity);
        // When
        MvcResult result = mockMvc.perform(
                        put("/api/cart/cart-items/{cartItemId}", cartItemId)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        System.out.println(actual);
        Assertions.assertEquals("Can't update Shopping Cart: CartItem with ID "
                + cartItemId + " not found.", actual);
    }

    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method updateCartItemQuantity is called by authorised existing user,
            Item data is null
            Then the status BAD REQUEST should be returned.
            """)
    public void updateCartItemQuantity_WithValidDataForExistingUser_ShouldReturnStatusBadRequest()
            throws Exception {
        // Given
        Long cartItemId = 1L;
        // When
        MvcResult result = mockMvc.perform(
                        put("/api/cart/cart-items/{cartItemId}", cartItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        System.out.println(actual);
        assertTrue(actual.contains("Failed to read request"));
    }

    @Test
    @DisplayName("""
            When method updateCartItemQuantity is called with non authorised user,
            Then the status Forbidden should be returned.
            """)
    public void updateCartItemQuantity_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long cartItemId = 1L;
        // When
        MvcResult result = mockMvc.perform(
                        put("/api/cart/cart-items/{cartItemId}", cartItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
        String actual = result.getResponse().getContentAsString();
        System.out.println(actual);
    }

    // Delete Book From Shopping Cart
    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method deleteBookFromShoppingCart is called by authorised existing user,
            Item is present in shopping cart
            Then the the status NO CONTENT should be returned.
            """)
    public void deleteBookFromShoppingCart_WithItemPresentInShoppingCart_ShouldReturnNoContent()
            throws Exception {
        // Given
        Long cartItemId = 4L;
        // When
        mockMvc.perform(
                        delete("/api/cart/cart-items/{cartItemId}", cartItemId)
                )
                .andExpect(status().isNoContent())
                .andReturn();
        // Then
        ShoppingCart actualCard = shoppingCartRepository
                .getShoppingCartByUserEmail("bob.jones@example.com");
        System.out.println(actualCard.getCartItems().size());
        Set<CartItem> actualItems = actualCard.getCartItems();
        System.out.println(actualItems);
    }

    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method deleteBookFromShoppingCart is called by authorised existing user,
            Item not present in shopping cart
            Then the status NOT FOUND should be returned.
            """)
    public void deleteBookFromShoppingCart_WithItemNotPresentInShoppCart_ShouldReturnShoppCartDto()
            throws Exception {
        Long cartItemId = 999L;
        // When
        MvcResult result = mockMvc.perform(
                        delete("/api/cart/cart-items/{cartItemId}", cartItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        System.out.println(actual);
        Assertions.assertEquals("Can't delete Shopping Cart: CartItem with ID "
                + cartItemId + " not found.", actual);
    }

    @Test
    @DisplayName("""
            When method deleteBookFromShoppingCart is called with non authorised user,
            Then the status Forbidden should be returned.
            """)
    public void deleteBookFromShoppingCart_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long cartItemId = 1L;
        // When
        mockMvc.perform(
                        delete("/api/cart/cart-items/{cartItemId}", cartItemId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }
}
