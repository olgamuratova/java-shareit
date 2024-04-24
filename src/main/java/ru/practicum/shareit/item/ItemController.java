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
import ru.practicum.shareit.item.check.CheckService;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private static final String OWNER = "X-Sharer-User-Id";

    private final ItemServiceImpl itemService;

    private final CheckService check;

    @Autowired
    public ItemController(ItemServiceImpl itemService, CheckService check) {
        this.itemService = itemService;
        this.check = check;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        return itemService.getItemById(itemId);
    }

    @ResponseBody
    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto, @RequestHeader(OWNER) Long ownerId) {
        ItemDto newItemDto = null;
        if (check.isExistUser(ownerId)) {
            newItemDto = itemService.create(itemDto, ownerId);
        }
        return newItemDto;
    }


    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader(OWNER) Long ownerId) {
        return itemService.getItemsByOwner(ownerId);
    }

    @ResponseBody
    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto, @PathVariable Long itemId,
                          @RequestHeader(OWNER) Long ownerId) {
        ItemDto newItemDto = null;
        if (check.isExistUser(ownerId)) {
            newItemDto = itemService.update(itemDto, ownerId, itemId);
        }
        return newItemDto;
    }

    @DeleteMapping("/{itemId}")
    public ItemDto delete(@PathVariable Long itemId, @RequestHeader(OWNER) Long ownerId) {
        return itemService.delete(itemId, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearchQuery(@RequestParam String text) {
        return itemService.getItemsBySearchQuery(text);
    }
}