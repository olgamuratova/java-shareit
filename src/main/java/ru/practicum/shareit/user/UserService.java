package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper mapper;

    private Long id = 1L;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.mapper = userMapper;
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(mapper::toUserDto)
                .collect(toList());
    }


    public UserDto getUserById(Long id) {
        return mapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден")));
    }

    public UserDto create(UserDto userDto) {
        validateUser(userDto);
        userDto.setId(id++);
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistException("Email уже используется");
        }
        return mapper.toUserDto(userRepository.save(mapper.toUser(userDto)));
    }

    public UserDto update(UserDto userDto, Long id) {
        if (userDto.getId() == null) {
            userDto.setId(id);
        }
        User user = userRepository.findById(id)
                .orElseThrow();
        User targetUser = mapper.mergeUser(user, userDto);
        if (userDto.getEmail() != null &&
                !user.getEmail().equalsIgnoreCase(userDto.getEmail()) &&
                userRepository.existsByEmail(targetUser.getEmail())) {
            throw new UserAlreadyExistException("Email уже используется");
        }
        return mapper.toUserDto(userRepository.save(targetUser));
    }

    public UserDto delete(Long userId) {
        UserDto user = getUserById(userId);
        userRepository.deleteById(userId);
        return user;
    }

    private void validateUser(UserDto user) {
        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
    }
}