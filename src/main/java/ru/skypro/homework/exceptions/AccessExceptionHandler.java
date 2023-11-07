package ru.skypro.homework.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AccessExceptionHandler {

    @ExceptionHandler(AccessErrorException.class)
    public ResponseEntity<?> handleNotFound(AccessErrorException e) {
        log.error("Нет прав на изменение объявления");
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("У Вас нет прав на изменение объявления!");
    }
}
