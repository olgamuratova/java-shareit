package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final UserMapper userMapper;

    @Transactional
    public BookingResponseDto create(BookingDto dto, Long bookerId) {
        validateBookingDates(dto.getStart(), dto.getEnd());
        Item item = getItem(dto.getItemId());
        validateItemAvailability(item, bookerId);

        User booker = userMapper.toUser(userService.getUserById(bookerId));
        Booking booking = BookingMapper.toBooking(dto, item, booker);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponseDto setApproved(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findBookingOwner(bookingId, userId);

        if (booking == null) {
            throw new BookingNotFoundException("Booking не найден");
        }
        if (approved) {
            validateBookingStatus(booking.getStatus(), Status.APPROVED);
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findBookingOwnerOrBooker(bookingId, userId);
        if (booking == null) {
            throw new BookingNotFoundException("Booking не найден");
        }
        return BookingMapper.toBookingDto(booking);
    }

    public List<BookingResponseDto> getAllReserve(Long userId, State state, String typeUser) {
        if (state == null) {
            state = State.ALL;
        }

        boolean isOwner = "owner".equals(typeUser);
        List<Booking> bookings = findBookingsByState(userId, state, isOwner);

        if (bookings.isEmpty()) {
            throw new IllegalArgumentException("Бронирование не найдено");
        }

        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.equals(end)) {
            throw new ItemNotAvailableException("Дата начала позже или равна окончанию бронирования");
        }
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new ItemNotFoundException("Item не найден"));
    }

    private void validateItemAvailability(Item item, Long bookerId) {
        if (!item.getIsAvailable()) {
            throw new ItemNotAvailableException("Вещь не доступна для бронирования");
        }

        if (item.getOwnerId().equals(bookerId)) {
            throw new BookingNotFoundException("Пользователь не может забронировать свою вещь");
        }
    }

    private void validateBookingStatus(Status currentStatus, Status expectedStatus) {
        if (currentStatus.equals(expectedStatus)) {
            throw new ItemNotAvailableException("Статус " + expectedStatus + " уже установлен");
        }
    }

    private List<Booking> findBookingsByState(Long userId, State state, boolean isOwner) {
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                return isOwner ?
                        bookingRepository.findAllByOwnerIdOrderByStartDesc(userId) :
                        bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case FUTURE:
                return isOwner ?
                        bookingRepository.findAllByOwnerIdAndStartAfterOrderByStartDesc(userId, now) :
                        bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING:
                return isOwner ?
                        bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING) :
                        bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
            case CURRENT:
                return isOwner ?
                        bookingRepository.findAllByOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now) :
                        bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStart(userId, now, now);
            case PAST:
                return isOwner ?
                        bookingRepository.findAllByOwnerIdAndEndBeforeOrderByStartDesc(userId, now) :
                        bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case REJECTED:
                return isOwner ?
                        bookingRepository.findAllByOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED) :
                        bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
            default:
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }
}

