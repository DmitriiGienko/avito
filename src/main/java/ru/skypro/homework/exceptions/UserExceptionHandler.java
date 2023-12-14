package ru.skypro.homework.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleNotFound(UserNotFoundException e) {
        log.error("Пользователь не найден");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Пользователь не найден");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleNotFound(ForbiddenException e) {
        log.error("Доступ запрещен!");
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("forbidden");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleNotFound(UserAlreadyExistsException e) {
        log.error("Пользователь с таким именем уже существует");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Пользователь с таким именем уже существует");
    }
}
