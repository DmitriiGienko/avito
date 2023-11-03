package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.projections.NewPassword;
import ru.skypro.homework.projections.UpdateUser;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.impl.UserServiceImpl;

import javax.validation.Valid;
import java.io.IOException;

@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;
    private final UserRepo userRepo; // так... потестить
    private final ImageService imageService;

    // Получение пользователя
    @GetMapping("/me")
    public UserDTO getUser() {
        return userService.getUser();
    }

    @PostMapping("/set_password")
    public void setPassword(@RequestBody @Valid NewPassword newPassword) {
        userService.updatePassword(newPassword);
    }

    @PatchMapping("/me")
    public UpdateUser updateUser(@RequestBody UpdateUser updateUser) {
        return userService.updateUser(updateUser);
    }

    @PatchMapping("/me/image")
    public ResponseEntity<String> updateUserImage(
            @RequestPart MultipartFile image) throws IOException {
        userService.updateUserImage(image);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getImage(@PathVariable("id") String id) {
        return imageService.getImage(id);
    }
}
