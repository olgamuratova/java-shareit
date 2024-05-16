package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.check.CheckService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private static final String OWNER = "X-Sharer-User-Id";

    private final ItemService itemService;

    private final CheckService check;

    @Autowired
    public ItemController(ItemService itemService, CheckService check) {
        this.itemService = itemService;
        this.check = check;
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItemById(@PathVariable Long itemId, @RequestHeader(name = OWNER) Long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @ResponseBody
    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto, @RequestHeader(name = OWNER) Long ownerId) {
        if (check.isExistUser(ownerId)) {
            return itemService.create(itemDto, ownerId);
        }
        throw new UserNotFoundException("Пользователь не найден");
    }


    @GetMapping
    public List<ItemResponseDto> getItemsByOwner(@RequestHeader(name = OWNER) Long ownerId) {
        return itemService.getItemsByOwner(ownerId);
    }

    @ResponseBody
    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto, @PathVariable Long itemId,
                          @RequestHeader(name = OWNER) Long ownerId) {
        if (check.isExistUser(ownerId)) {
            return itemService.update(itemDto, ownerId, itemId);
        }
        throw new UserNotFoundException("Пользователь не найден");
    }

    @DeleteMapping("/{itemId}")
    public ItemDto delete(@PathVariable Long itemId, @RequestHeader(name = OWNER) Long ownerId) {
        return itemService.delete(itemId, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearchQuery(@RequestParam String text) {
        return itemService.getItemsBySearchQuery(text);
    }

    @PostMapping("/{itemId}/comment")
    public Comment addComment(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                              @PathVariable("itemId") Long itemId,
                              @Valid @RequestBody CommentDto comment) {
        return itemService.createComment(comment, userId, itemId);
    }
}