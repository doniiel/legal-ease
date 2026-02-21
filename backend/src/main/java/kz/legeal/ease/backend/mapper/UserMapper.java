package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.dto.BaseUserDto;
import kz.legeal.ease.backend.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    BaseUserDto toBaseDto(User user);

    UserDto toDto(User user);
}
