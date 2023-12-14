package ru.skypro.homework.projections;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@Data
public class NewPassword {

    @NotBlank(message = "Обязательное поле")
    @Size(min = 8, max = 16, message = "Количество символов от 8 до 16")
    private   String currentPassword;

    @Size(min = 2, max = 16, message = "Количество символов от 2 до 16")
    @Size(min = 8, max = 16, message = "Количество символов от 8 до 16")
    private   String newPassword;
}
