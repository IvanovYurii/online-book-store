package ivanov.springbootintro.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ivanov.springbootintro.dto.order.OrderDto;
import ivanov.springbootintro.dto.order.PlaceOrderRequestDto;
import ivanov.springbootintro.dto.order.UpdateStatusOrderRequestDto;
import ivanov.springbootintro.dto.orderitems.OrderItemDto;
import ivanov.springbootintro.dto.shoppingcart.ShoppingCartDto;
import ivanov.springbootintro.mapper.OrderItemMapper;
import ivanov.springbootintro.mapper.OrderMapper;
import ivanov.springbootintro.mapper.ShoppingCartMapper;
import ivanov.springbootintro.model.Order;
import ivanov.springbootintro.model.ShoppingCart;
import ivanov.springbootintro.model.User;
import ivanov.springbootintro.repository.order.OrderRepository;
import ivanov.springbootintro.repository.orderitem.OrderItemRepository;
import ivanov.springbootintro.repository.shoppingcart.ShoppingCartRepository;
import ivanov.springbootintro.repository.user.UserRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;

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
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/cart_items/add-default-cart-items.sql")
            );
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    // Place Order
    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method placeOrder is called with authorised existing user,
            Then the corresponding OrderDto should be returned.
            """)
    public void placeOrder_WithExistingUser_ShouldReturnOrderDto()
            throws Exception {
        // Given
        User user = userRepository.findByEmail("bob.jones@example.com").orElseThrow();
        PlaceOrderRequestDto placeOrderRequestDto = new PlaceOrderRequestDto(
                user.getShippingAddress());
        ShoppingCart expectedCard = shoppingCartRepository
                .getShoppingCartByUserEmail("bob.jones@example.com");
        ShoppingCartDto shoppingCartDto = shoppingCartMapper.toDto(expectedCard);
        String jsonRequest = objectMapper.writeValueAsString(placeOrderRequestDto);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/orders")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        Assertions.assertNotNull(result);
        OrderDto actualOrder = objectMapper.readValue(result.getResponse()
                .getContentAsString(), OrderDto.class);
        Assertions.assertEquals(shoppingCartDto.cartItems().size(),
                actualOrder.orderItems().size());
        ShoppingCart actualCard = shoppingCartRepository
                .getShoppingCartByUserEmail("bob.jones@example.com");
        Assertions.assertEquals(0, actualCard.getCartItems().size());
    }

    @Test
    @WithUserDetails("jack.jones@example.com")
    @DisplayName("""
            When method placeOrder is called with authorised user,
            ShoppingCart is empty
            Then the status NOT FOUND should be returned.
            """)
    public void placeOrder_WithEmptyShoppingCart_ShouldReturnStatusNotFound()
            throws Exception {
        // Given
        User user = userRepository.findByEmail("jack.jones@example.com").orElseThrow();
        PlaceOrderRequestDto placeOrderRequestDto = new PlaceOrderRequestDto(
                user.getShippingAddress()
        );
        String jsonRequest = objectMapper.writeValueAsString(placeOrderRequestDto);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/orders")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertEquals("Shopping Cart with email " + user.getEmail()
                + " is empty", actual);
    }

    @Test
    @WithUserDetails("jack.jones@example.com")
    @DisplayName("""
            When method placeOrder is called with authorised user,
            PlaceOrderRequestDto is empty
            Then the status BAD REQUEST should be returned.
            """)
    public void placeOrder_WithEmptyRequest_ShouldReturnStatusBadRequest()
            throws Exception {
        // Given
        PlaceOrderRequestDto placeOrderRequestDto = new PlaceOrderRequestDto(
                ""
        );
        String jsonRequest = objectMapper.writeValueAsString(placeOrderRequestDto);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/orders")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.contains("must not be empty"));
    }

    @Test
    @WithUserDetails("jack.jones@example.com")
    @DisplayName("""
            When method placeOrder is called with authorised user,
            PlaceOrderRequestDto is null
            Then the status BAD REQUEST should be returned.
            """)
    public void placeOrder_WithNullPlaceOrderRequestDto_ShouldReturnStatusBadRequest()
            throws Exception {
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.contains("Failed to read request"));
    }

    @Test
    @DisplayName("""
            When method placeOrder is called with non authorised user,
            Then the status Forbidden should be returned.
            """)
    public void placeOrder_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // When
        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    // Get Order History
    @Test
    @Transactional
    @WithUserDetails("john.doe@example.com")
    @DisplayName("""
            When method getOrderHistory is called with authorised user,
            Order History not empty
            Then the list OrderDto should be returned.
            """)
    public void getOrderHistory_WithNotEmptyOrderHistory_ShouldReturnListOrderDto()
            throws Exception {
        // Given
        User user = userRepository.findByEmail("john.doe@example.com").orElseThrow();
        List<OrderDto> expected = orderRepository.findByUserId(user.getId()).stream()
                .map(orderMapper::orderToOrderDto)
                .toList();
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        List<OrderDto> actual = objectMapper.readValue(result
                .getResponse()
                .getContentAsString(), new TypeReference<>() {
                });
        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertTrue(expected.containsAll(actual));
    }

    @Test
    @WithUserDetails("jack.jones@example.com")
    @DisplayName("""
            When method getOrderHistory is called with authorised user,
            Order History is empty
            Then the Empty list OrderDto should be returned.
            """)
    public void getOrderHistory_WithEmptyOrderHistory_ShouldReturnEmptyListOrderDto()
            throws Exception {
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        OrderDto[] actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), OrderDto[].class);
        Assertions.assertEquals(0, actual.length);
    }

    @Test
    @DisplayName("""
            When method getOrderHistory is called with non authorised user,
            Then the status Forbidden should be returned.
            """)
    public void getOrderHistory_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // When
        mockMvc.perform(
                        get("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    // Update Order Status
    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method updateOrderStatus is called by authorised user with role ADMIN,
            Order History not empty
            Then the OrderDto with update status should be returned.
            """)
    public void updateOrderStatus_WithUserRoleAdmin_ShouldReturnOrderDto()
            throws Exception {
        // Given
        int id = 1;
        User user = userRepository.findByEmail("john.doe@example.com").orElseThrow();
        UpdateStatusOrderRequestDto request = new UpdateStatusOrderRequestDto("DELIVERED");
        String jsonRequest = objectMapper.writeValueAsString(request);
        // When
        MvcResult result = mockMvc.perform(
                        patch("/api/orders/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        OrderDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), OrderDto.class);
        List<Order> expectedListOrder = orderRepository.findByUserId(user.getId());
        Assertions.assertEquals(expectedListOrder.get(id - 1).getStatus(), actual.status());
    }

    @Test
    @WithUserDetails("bob.jones@example.com")
    @DisplayName("""
            When method updateOrderStatus is called by authorised user without role ADMIN,
            With invalid Order ID
            Then the status NOT FOUND should be returned.
            """)
    public void updateOrderStatus_WithInvalidOrderId_ShouldReturnStatusNotFound()
            throws Exception {
        // Given
        Long id = 999L;
        UpdateStatusOrderRequestDto request = new UpdateStatusOrderRequestDto("DELIVERED");
        String jsonRequest = objectMapper.writeValueAsString(request);
        // When
        MvcResult result = mockMvc.perform(
                        patch("/api/orders/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertEquals("Can't find order by id=" + id, actual);
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    @DisplayName("""
            When method updateOrderStatus is called by authorised user without role ADMIN,
            Order History not empty
            Then the status FORBIDDEN should be returned.
            """)
    public void updateOrderStatus_WithUserRoleNotAdmin_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long id = 1L;
        UpdateStatusOrderRequestDto request = new UpdateStatusOrderRequestDto("DELIVERED");
        String jsonRequest = objectMapper.writeValueAsString(request);
        // When
        mockMvc.perform(
                        patch("/api/orders/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("""
            When method updateOrderStatus is called with non authorised user,
            Then the status Forbidden should be returned.
            """)
    public void updateOrderStatus_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long id = 1L;
        UpdateStatusOrderRequestDto request = new UpdateStatusOrderRequestDto("DELIVERED");
        String jsonRequest = objectMapper.writeValueAsString(request);
        // When
        mockMvc.perform(
                        patch("/api/orders/{id}", id)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    // Get Order Items
    @Test
    @WithUserDetails("john.doe@example.com")
    @Transactional
    @DisplayName("""
            When method getOrderHistory is called with authorised user,
            Order not empty
            Then the list OrderItemDto should be returned.
            """)
    public void getOrderItems_WithNotEmptyOrder_ShouldReturnListOrderItemDto()
            throws Exception {
        // Given
        Long orderId = 1L;
        Order order = orderRepository.findById(orderId)
                .orElse(null);
        assert order != null;
        List<OrderItemDto> expected = order.getOrderItems().stream()
                .map(orderItemMapper::orderItemToDto)
                .toList();
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/orders/{orderId}/items", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        List<OrderItemDto> actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), new TypeReference<>() {
                });
        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertTrue(expected.containsAll(actual));
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    @DisplayName("""
            When method getOrderHistory is called with authorised user,
            Order not present
            Then the status NOT FOUND should be returned.
            """)
    public void getOrderItems_WithInvalidOrderId_ShouldReturnStatusNotFound()
            throws Exception {
        // Given
        Long orderId = 999L;
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/orders/{orderId}/items", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertEquals("Can't find order by id=" + orderId, actual);
    }

    @Test
    @DisplayName("""
            When method getOrderItems is called with non authorised user,
            Then the status Forbidden should be returned.
            """)
    public void getOrderItems_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long orderId = 1L;
        // When
        mockMvc.perform(
                        get("/api/orders/{orderId}/items", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }

    // Get Order Item
    @Test
    @WithUserDetails("john.doe@example.com")
    @Transactional
    @DisplayName("""
            When method getOrderHistory is called with authorised user,
            Order not empty
            Then the list OrderItemDto should be returned.
            """)
    public void getOrderItem_WithNotEmptyOrder_ShouldReturnListOrderItemDto()
            throws Exception {
        // Given
        Long orderId = 1L;
        Long itemId = 1L;
        OrderItemDto expected = orderItemMapper.orderItemToDto(orderItemRepository
                .findByIdAndOrderId(orderId, itemId).orElseThrow());
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/orders/{orderId}/items/{itemId}", orderId, itemId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        OrderItemDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), OrderItemDto.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    @DisplayName("""
            When method getOrderHistory is called with authorised user,
            Order not present
            Then the status NOT FOUND should be returned.
            """)
    public void getOrderItem_WithInvalidOrderId_ShouldReturnStatusNotFound()
            throws Exception {
        // Given
        Long orderId = 999L;
        Long itemId = 1L;
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/orders/{orderId}/items/{itemId}", orderId, itemId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertEquals("Can't find order by id=" + orderId, actual);
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    @DisplayName("""
            When method getOrderHistory is called with authorised user,
            OrderItem not present
            Then the status NOT FOUND should be returned.
            """)
    public void getOrderItem_WithInvalidItemId_ShouldReturnStatusNotFound()
            throws Exception {
        // Given
        Long orderId = 1L;
        Long itemId = 999L;
        // When
        MvcResult result = mockMvc.perform(
                        get("/api/orders/{orderId}/items/{itemId}", orderId, itemId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertEquals("Can't find order item with id=" + itemId
                + " in order id=" + orderId, actual);
    }

    @Test
    @DisplayName("""
            When method getOrderItem is called with non authorised user,
            Then the status Forbidden should be returned.
            """)
    public void getOrderItem_WithNonAuthorisedUser_ShouldReturnStatusForbidden()
            throws Exception {
        // Given
        Long orderId = 1L;
        // When
        mockMvc.perform(
                        get("/api/orders/{orderId}/items", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
    }
}
