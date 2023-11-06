package ru.skypro.homework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AccessExceptionHandler {

    @ExceptionHandler(AccessErrorException.class)
    public ResponseEntity<?> handleNotFound(AccessErrorException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("У Вас нет прав на изменение объявления!");
    }
}
