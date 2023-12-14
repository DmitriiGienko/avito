package ru.skypro.homework.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ImageExceptionHandler {

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ImageNotFoundException e) {
        log.error("Картинок не найдено!");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Картинок не найдено!");
    }
}
