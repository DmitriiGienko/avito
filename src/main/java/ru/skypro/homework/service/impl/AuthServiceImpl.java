package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.exceptions.UserAlreadyExistsException;
import ru.skypro.homework.projections.Login;
import ru.skypro.homework.projections.Register;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.UserServiceSecurity;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserServiceSecurity manager;
    private final PasswordEncoder encoder;
    private final UserRepo userRepo;

    /**
     * Метод авторизации пользователя
     */
    @Override
    public boolean validLogin(String userName, String password) {
        if (manager.loadUserByUsername(userName) == null) {
            return false;
        }
        log.info("Пользователь {} авторизирован", userName);
        UserDetails userDetails = manager.loadUserByUsername(userName);
        return encoder.matches(password, userDetails.getPassword());
    }

    /**
     * Проверка возможности регистрации пользователя
     */
    @Override
    public boolean validRegister(Register register) {
        return register.getUsername() != null && !register.getUsername().isBlank()
                && register.getFirstName() != null && !register.getFirstName().isBlank()
                && register.getLastName() != null && !register.getLastName().isBlank()
                && register.getPhone() != null && !register.getPhone().isBlank()
                && register.getPassword() != null && !register.getPassword().isBlank();
    }

    /**
     * Регистрация пользователя
     */
    @Override
    public ResponseEntity<?> getRegistration(Register register) {
        if (userRepo.findByUserName(register.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        if (validRegister(register)) {
            manager.createUser(register);
            log.info("Пользователь {} зарегистрирован", register.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Авторизация пользователя
     */
    @Override
    public ResponseEntity<?> getLogin(Login login) {
        if (validLogin(login.getUsername(), login.getPassword())) {
            return ResponseEntity.ok().build();
        } else {
            log.error("Ошибка аутентификации - {}", login.getUsername());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


}
