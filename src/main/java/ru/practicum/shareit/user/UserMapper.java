package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getName(),
                user.getEmail(),
                user.getId()
        );
    }

    public User toUser(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }

    public User mergeUser(User user, UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName() == null ? user.getName() : userDto.getName(),
                userDto.getEmail() == null ? user.getEmail() : userDto.getEmail()
        );
    }
}
