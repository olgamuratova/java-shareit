package ru.practicum.shareit.user.dto;

import lombok.Data;

@Data
public class UserDto {

    private Long id;

    private String name;

    private String email;

    public UserDto(String name, String email, Long id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }
}
