package ivanov.springbootintro.service;

import ivanov.springbootintro.dto.user.UserRegistrationRequestDto;
import ivanov.springbootintro.dto.user.UserResponseDto;
import ivanov.springbootintro.exception.RegistrationException;

public interface UserService {
    UserResponseDto save(UserRegistrationRequestDto requestDto)
            throws RegistrationException;
}
