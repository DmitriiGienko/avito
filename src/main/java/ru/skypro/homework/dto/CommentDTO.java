package ru.skypro.homework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
/**
 * Класс комментарий DTO
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentDTO {

    @NotBlank(message = "Обязательное поле")
    private int pk;

    @NotBlank(message = "Обязательное поле")
    private int author;

    private String authorImage;

    @NotBlank(message = "Обязательное поле")
    @Size(min = 2, max = 16, message = "Количество символов от 2 до 16")
    private String authorFirstName;

    @PastOrPresent(message = "Неверный формат даты")
    private LocalDateTime createdAt;

    @NotBlank(message = "Обязательное поле")
    @Size(min = 8, max = 64, message = "Количество символов от 8 до 64")
    private String text;
}
