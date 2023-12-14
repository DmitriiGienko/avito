package ru.skypro.homework.service;

import org.springframework.http.ResponseEntity;
import ru.skypro.homework.projections.Login;
import ru.skypro.homework.projections.Register;

public interface AuthService {
    boolean validLogin(String userName, String password);

    boolean validRegister(Register register);

    ResponseEntity<?> getRegistration(Register register);

    ResponseEntity<?> getLogin(Login login);
}
