package ru.skypro.homework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
/**
 * Класс пользователя DTO
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class UserDTO {

    @NotBlank(message = "Обязательное поле")
    private int id;

    @NotBlank(message = "Обязательное поле")
    @Email(message = "Неверный формат адреса email")
    private String email;

    @NotBlank(message = "Обязательное поле")
    @Size(min = 2, max = 16, message = "Количество символов от 2 до 16")
    private String firstName;

    @NotBlank(message = "Обязательное поле")
    @Size(min = 2, max = 16, message = "Количество символов от 2 до 16")
    private String lastName;

    @NotBlank(message = "Обязательное поле")
    @Pattern(regexp = ("\\+7\\s?\\(?\\d{3}\\)?\\s?\\d{3}-?\\d{2}-?\\d{2}"),
            message = "Укажите номер в формате +7----------")
    private String phone;
    private String role;
    private String image;

}
