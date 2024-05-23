package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingDtoItem {

    private Long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private Long bookerId;

    private Status status;
}
