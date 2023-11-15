package ivanov.springbootintro.service.impl;

import ivanov.springbootintro.dto.user.UserRegistrationRequestDto;
import ivanov.springbootintro.dto.user.UserResponseDto;
import ivanov.springbootintro.exception.RegistrationException;
import ivanov.springbootintro.mapper.UserMapper;
import ivanov.springbootintro.model.User;
import ivanov.springbootintro.repository.user.UserRepository;
import ivanov.springbootintro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto save(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RegistrationException("User is all ready registered");
        }
        User user = userMapper.toModel(requestDto);
        return userMapper.toDto(userRepository.save(user));
    }
}
