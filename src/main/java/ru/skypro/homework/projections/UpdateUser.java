package ru.skypro.homework.projections;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@RequiredArgsConstructor
@Data
public class UpdateUser {

    @NotBlank(message = "Обязательное поле")
    @Size(min = 3, max = 10, message = "Количество символов от 3 до 10")
    private final String firstName;

    @NotBlank(message = "Обязательное поле")
    @Size(max = 10, min = 3, message = "Количество символов от 3 до 10")
    private final String lastName;

    @NotBlank(message = "Обязательное поле")
    @Pattern(regexp = ("\\+7\\s?\\(?\\d{3}\\)?\\s?\\d{3}-?\\d{2}-?\\d{2}"),
            message = "Укажите номер в формате +7----------")
    private final String phone;
}
