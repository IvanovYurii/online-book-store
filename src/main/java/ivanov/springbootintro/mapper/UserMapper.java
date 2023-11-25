package ivanov.springbootintro.mapper;

import ivanov.springbootintro.config.MapperConfig;
import ivanov.springbootintro.dto.user.UserRegistrationRequestDto;
import ivanov.springbootintro.dto.user.UserResponseDto;
import ivanov.springbootintro.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toDto(User user);

    User toEntity(UserRegistrationRequestDto requestDto);
}
