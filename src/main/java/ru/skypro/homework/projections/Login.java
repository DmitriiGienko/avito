package ru.skypro.homework.projections;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class Login {

    @NotBlank(message = "Обязательное поле")
    @Size(min = 8, max = 16, message = "Количество символов от 8 до 16")
    private String username;

    @NotBlank(message = "Обязательное поле")
    @Size(min = 4, max = 32, message = "Количество символов от 8 до 16")
    private String password;
}
