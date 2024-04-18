package ivanov.springbootintro.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ivanov.springbootintro.dto.user.UserRegistrationRequestDto;
import ivanov.springbootintro.exception.RegistrationException;
import ivanov.springbootintro.mapper.UserMapper;
import ivanov.springbootintro.model.Role;
import ivanov.springbootintro.model.User;
import ivanov.springbootintro.repository.user.UserRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRegistrationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@email.com");
        user.setPassword("password");
        user.setFirstName("Bob");
        user.setLastName("Bob ik");
        user.setShippingAddress("Freedom St. App 25");
        Role userRole = new Role();
        userRole.setName(Role.RoleName.ROLE_USER);
        user.setRoles(Collections.singleton(userRole));

        requestDto = new UserRegistrationRequestDto();
        requestDto.setFirstName("Bob");
        requestDto.setLastName("Bob ik");
        requestDto.setShippingAddress("Freedom St. App 25");
        requestDto.setPassword("password");
        requestDto.setRepeatPassword("password");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    // Registration
    @Test
    @DisplayName("""
            Given a user all ready registered,
            When register is called,
            Then a RegistrationException should be thrown.
            """)
    public void registerUser_WithUserAlreadyRegistered_ShouldThrowRegistrationException() {
        // Given
        when(userRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(user));
        // When, Then
        String expectedMessage = "User is all ready registered";
        RegistrationException actualException = assertThrows(
                RegistrationException.class,
                () -> userService.register(requestDto));
        assertEquals(expectedMessage, actualException.getMessage());

        verify(userRepository, times(1)).findByEmail(requestDto.getEmail());
        verifyNoMoreInteractions(userRepository);
    }
}
