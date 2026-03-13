package kz.legeal.ease.backend.mapper;

import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(source = "phone", target = "telephone")
    @Mapping(target = "userProfileImageUrl", ignore = true)
    UserDto toDto(User user);
}