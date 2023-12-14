package ru.skypro.homework.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AdExceptionHandler {

    @ExceptionHandler(AdNotFoundException.class)
    public ResponseEntity<?> handleNotFound(AdNotFoundException e) {
        log.error("Объявление не найдено");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Not found");
    }
}
