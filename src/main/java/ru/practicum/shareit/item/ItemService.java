package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemStorage itemStorage;

    private final UserService userService;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    private final ItemRepository itemRepository;

    private final ItemMapper mapper;

    @Autowired
    public ItemService(ItemStorage itemStorage,
                       UserService userService,
                       BookingRepository bookingRepository,
                       CommentRepository commentRepository,
                       ItemRepository itemRepository,
                       ItemMapper itemMapper) {
        this.itemStorage = itemStorage;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRepository = itemRepository;
        this.mapper = itemMapper;
    }

    public ItemDto create(ItemDto itemDto, Long ownerId) {
        return mapper.toItemDto(itemRepository.save(mapper.toItem(itemDto, ownerId)));
    }

    public List<ItemResponseDto> getItemsByOwner(Long ownderId) {
        UserDto user = userService.getUserById(ownderId);
        List<Item> items = itemRepository.findByOwnerId(ownderId);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        List<Booking> booking = bookingRepository.findAllByOwnerIdAndItemIn(ownderId, itemIds);
        List<Comment> comment = commentRepository.findAllByAndAuthorName(user.getName());
        return items.stream()
                .map(item -> mapper.toItemResponseDto(item, booking, comment))
                .collect(Collectors.toList());
    }

    public ItemResponseDto getItemById(Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("У пользователя вещь отсутствует"));
        List<Booking> booking = bookingRepository.findAllByItemIdAndOwnerId(id, userId);
        List<Comment> comment = commentRepository.findAllByItemId(id);
        return mapper.toItemResponseDto(item, booking, comment);
    }

    public ItemDto update(ItemDto itemDto, Long ownerId, Long itemId) {
        if (itemDto.getId() == null) {
            itemDto.setId(itemId);
        }
        Item oldItem = itemRepository.findById(itemId).orElse(new Item());
        if (!oldItem.getOwnerId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя вещь отсутствует");
        }

        return mapper.toItemDto(itemRepository.save(mapper.mergeItem(oldItem, itemDto, ownerId)));
    }

    public ItemDto delete(Long itemId, Long ownerId) {
        Item item = itemRepository.findById(itemId).orElse(new Item());
        if (!item.getOwnerId().equals(ownerId)) {
            throw new ItemNotFoundException("У пользователя вещь отсутствует");
        }
        itemRepository.deleteById(itemId);
        return mapper.toItemDto(item);
    }

    public void deleteItemsByOwner(Long ownderId) {
        itemRepository.deleteItemsByOwnerId(ownderId);
    }

    public List<ItemDto> getItemsBySearchQuery(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> itemList =
                itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndIsAvailable(
                        text, text, true);
        return itemList.stream().map(mapper::toItemDto).collect(Collectors.toList());
    }

    @Transactional
    public Comment createComment(CommentDto dto, Long userId, Long itemId) {
        List<Booking> booking = bookingRepository.findAllByBookerIdAndItemIdAndStatusNotAndStartBefore(userId, itemId, Status.REJECTED, LocalDateTime.now());
        if (booking.isEmpty()) {
            throw new ValidationException("Вы не можете оставить отзыв, т.к. не бронировали вещь");
        }
        UserDto user = userService.getUserById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ValidationException("Item не найден"));
        Comment comment = CommentMapper.toComment(dto, user, item);

        return commentRepository.save(comment);
    }
}