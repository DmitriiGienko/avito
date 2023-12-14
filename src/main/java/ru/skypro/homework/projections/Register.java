package ru.skypro.homework.projections;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.skypro.homework.model.Role;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class Register {

    @NotBlank(message = "Обязательное поле")
    @Size(min = 4, max = 32, message = "Количество символов от 4 до 32")
    private String username;

    @NotBlank(message = "Обязательное поле")
    @Size(min = 8, max = 16, message = "Количество символов от 8 до 16")
    private String password;

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

    @NotNull
    private Role role;
}
