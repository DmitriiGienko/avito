package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.projections.NewPassword;
import ru.skypro.homework.projections.UpdateUser;
import ru.skypro.homework.service.impl.UserServiceImpl;

import javax.validation.Valid;

@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    /**
     * Получение пользователя
     */
    @GetMapping("/me")
    public UserDTO getUser() {
        return userService.getUser();
    }

    /**
     * Изменение пароля пользователя
     */
    @PostMapping("/set_password")
    public void setPassword(@RequestBody @Valid NewPassword newPassword) {
        userService.updatePassword(newPassword);
    }

    /**
     * Изменение пользователя
     */
    @PatchMapping("/me")
    public UpdateUser updateUser(@RequestBody @Valid UpdateUser updateUser) {
        return userService.updateUser(updateUser);
    }


}
