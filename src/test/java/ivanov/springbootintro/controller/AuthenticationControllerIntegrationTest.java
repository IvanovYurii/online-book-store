package ivanov.springbootintro.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ivanov.springbootintro.dto.user.UserLoginRequestDto;
import ivanov.springbootintro.dto.user.UserRegistrationRequestDto;
import ivanov.springbootintro.dto.user.UserResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerIntegrationTest {
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationRequestDto addUserData() {
        return new UserRegistrationRequestDto()
                .setEmail("admin@example.com")
                .setPassword("securePasswordAdmin")
                .setRepeatPassword("securePasswordAdmin")
                .setFirstName("Alice")
                .setLastName("Doe")
                .setShippingAddress("123 Main St, City, Country");
    }

    private UserLoginRequestDto loginUserData() {
        return new UserLoginRequestDto("bob.jones@example.com", "bobspassword");

    }

    @BeforeEach
    void beforeEach(
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    //User login
    @Test
    @DisplayName("""
            When method login is called with email and password for existing user,
            Then returns user Bearer token.
            """)
    public void loginUser_WithExistingUser_ShouldReturnBearerToken() throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(loginUserData());
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.contains("token"));
    }

    @Test
    @DisplayName("""
            When method login is called with invalid email and password,
            Then the status Forbidden should be returned.
            """)
    public void loginUser_WithInvalidUserData_ShouldReturnForbiddenStatus() throws Exception {
        // Given
        UserLoginRequestDto expected = new UserLoginRequestDto("wqew@dsg.at", "password");
        String jsonRequest = objectMapper.writeValueAsString(expected);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.contains("User login or password incorrect"));
    }

    @Test
    @DisplayName("""
            When method login is called with null email or password,
            Then the status BAD REQUEST should be returned.
            """)
    public void loginUser_WithNullUserData_ShouldReturnBadRequestStatus() throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(new UserLoginRequestDto(null, null));
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
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
    @DisplayName("""
            When method login is called with empty email or password,
            Then the status BAD REQUEST should be returned.
            """)
    public void loginUser_WithEmptyUserData_ShouldReturnBadRequestStatus() throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(new UserLoginRequestDto("", ""));
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.contains("errors"));
    }

    //User registration
    @Test
    @DisplayName("""
            When method registration is called with valid request data user,
            Then the UserDto should be returned.
            """)
    public void registrationUser_WithValidUserData_ShouldReturnUserDto() throws Exception {
        // Given
        String jsonRequest = objectMapper.writeValueAsString(addUserData());
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        // Then
        UserResponseDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), UserResponseDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(addUserData().getEmail(), actual.email());
        Assertions.assertEquals(addUserData().getFirstName(), actual.firstName());
        Assertions.assertEquals(addUserData().getLastName(), actual.lastName());
        Assertions.assertEquals(addUserData().getShippingAddress(), actual.shippingAddress());
    }

    @Test
    @DisplayName("""
            When method registration is called with invalid email,
            Then the status BAD REQUEST should be returned.
            """)
    public void registrationUser_WithInvalidUserEmail_ShouldReturnBadRequestStatus()
            throws Exception {
        // Given
        UserRegistrationRequestDto user = addUserData();
        user.setEmail("invalid_email");
        String jsonRequest = objectMapper.writeValueAsString(user);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.contains("email must be a well-formed email address"));
    }

    @Test
    @DisplayName("""
            When method registration is called with password not mismatch,
            Then the status BAD REQUEST should be returned.
            """)
    public void registrationUser_WithPasswordNotMismatch_ShouldReturnBadRequestStatus()
            throws Exception {
        // Given
        UserRegistrationRequestDto user = addUserData();
        user.setRepeatPassword("12345678");
        String jsonRequest = objectMapper.writeValueAsString(user);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.contains("Password and repeat password must match"));
    }

    @Test
    @DisplayName("""
            When method registration is called with password not containing more than
            eight and less than twenty symbols,
            Then the status BAD REQUEST should be returned.
            """)
    public void registrationUser_PasswordNotContainingMoreThan8AndLess20Symbols_ReturnBadRequest()
            throws Exception {
        // Given
        UserRegistrationRequestDto user = addUserData();
        user.setPassword("123456");
        user.setRepeatPassword("123456");
        String jsonRequest = objectMapper.writeValueAsString(user);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.contains("length must be between 8 and 20"));
    }

    @Test
    @DisplayName("""
            When method registration is called with null or empty user data,
            Then the status BAD REQUEST should be returned.
            """)
    public void registrationUser_WithNullOrEmptyUserData_ShouldReturnBadRequestStatus()
            throws Exception {
        // Given
        UserRegistrationRequestDto user = addUserData();
        user.setFirstName(null);
        user.setLastName("");
        String jsonRequest = objectMapper.writeValueAsString(user);
        // When
        MvcResult result = mockMvc.perform(
                        post("/api/auth/registration")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        // Then
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.contains("must not be empty"));
    }
}
