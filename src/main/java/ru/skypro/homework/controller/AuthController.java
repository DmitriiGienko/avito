package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.projections.Login;
import ru.skypro.homework.projections.Register;
import ru.skypro.homework.service.impl.AuthServiceImpl;

import javax.validation.Valid;


@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    /**
     * Авторизация пользователя
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid Login login) {
            return authService.getLogin(login);
    }


    /**
     * Регистрация пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid Register register) {
       return authService.getRegistration(register);
    }
}
