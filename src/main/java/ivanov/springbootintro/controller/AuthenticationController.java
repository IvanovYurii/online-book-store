package ivanov.springbootintro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ivanov.springbootintro.dto.user.UserLoginRequestDto;
import ivanov.springbootintro.dto.user.UserLoginResponseDto;
import ivanov.springbootintro.dto.user.UserRegistrationRequestDto;
import ivanov.springbootintro.dto.user.UserResponseDto;
import ivanov.springbootintro.exception.RegistrationException;
import ivanov.springbootintro.security.AuthenticationService;
import ivanov.springbootintro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for managing user authentication "
        + "and registration.")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/auth")
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "Login user",
            description = "Endpoint to authenticate a user using "
            + "email and password. Returns user Bearer token."
    )
    public UserLoginResponseDto login(
            @RequestBody @Valid UserLoginRequestDto requestDto
    ) {
        return authenticationService.authenticate(requestDto);
    }

    @PostMapping("/registration")
    @Operation(summary = "Registration a new user",
            description = "Endpoint to register a new user with the email, password, firstName, "
                    + "lastName, shippingAddress. Returns details of the registered user."
    )
    public UserResponseDto registerUser(@RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        return userService.register(requestDto);
    }
}
