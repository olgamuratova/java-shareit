package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class InMemoryItemStorage implements ItemStorage {

    public Map<Long, Item> items;
    private Long currentId;

    public InMemoryItemStorage() {
        currentId = 0L;
        items = new HashMap<>();
    }

    @Override
    public Item create(Item item) {

        if (isValidItem(item)) {
            item.setId(++currentId);
            items.put(item.getId(), item);
        }
        return item;
    }

    @Override
    public Item update(Item item) {

        Long itemId = item.getId();
        if (itemId == null) {
            throw new ValidationException("Идентификатор не может быть пустым");
        }
        Item existingItem = items.get(itemId);
        if (existingItem == null) {
            throw new ItemNotFoundException("Вещь с идентификатором " + itemId + " не найдена");
        }
        if (item.getName() == null) {
            item.setName(existingItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(existingItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(existingItem.getAvailable());
        }
        if (isValidItem(item)) {
            items.put(itemId, item);
        }
        return item;
    }

    @Override
    public Item delete(Long itemId) {

        if (itemId == null) {
            throw new ValidationException("Не может быть пустым");
        }
        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Вещь с идентификатором " + itemId + " не найдена");
        }
        return items.remove(itemId);
    }

    @Override
    public List<Item> getItemsByOwner(Long ownerId) {

        return items.values().stream()
                .filter(item -> ownerId.equals(item.getOwnerId()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItemsByOwner(Long ownerId) {

        List<Long> deleteIds = items.values().stream()
                .map(Item::getOwnerId)
                .filter(id -> id.equals(ownerId)).collect(Collectors.toList());
        for (Long deleteId : deleteIds) {
            items.remove(deleteId);
        }
    }

    @Override
    public Item getItemById(Long itemId) {

        if (!items.containsKey(itemId)) {
            throw new ItemNotFoundException("Вещь с идентификатором " + itemId + " не найдена");
        }
        return items.get(itemId);
    }

    @Override
    public List<Item> getItemsBySearchQuery(String text) {

        List<Item> searchItems = new ArrayList<>();
        if (!text.isBlank()) {
            searchItems = items.values().stream()
                    .filter(Item::getAvailable)
                    .filter(item -> item.getName().toLowerCase().contains(text) ||
                            item.getDescription().toLowerCase().contains(text))
                    .collect(toList());
        }
        return searchItems;
    }

    private boolean isValidItem(Item item) {
        if (item.getName() == null || item.getName().isEmpty() ||
                item.getDescription() == null || item.getDescription().isEmpty() ||
                item.getAvailable() == null) {
            throw new ValidationException("У вещи некорректные данные");
        }
        return true;
    }
}