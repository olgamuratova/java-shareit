package ru.practicum.shareit.item.check;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.user.UserServiceImpl;

@Service
public class CheckService {
    private UserServiceImpl userService;
    private ItemServiceImpl itemService;

    @Autowired
    public CheckService(UserServiceImpl userService, ItemServiceImpl itemService) {
        this.userService = userService;
        this.itemService = itemService;
    }

    public boolean isExistUser(Long userId) {
        boolean exist = false;
        if (userService.getUserById(userId) != null) {
            exist = true;
        }
        return exist;
    }

    public void deleteItemsByUser(Long userId) {
        itemService.deleteItemsByOwner(userId);
    }
}
