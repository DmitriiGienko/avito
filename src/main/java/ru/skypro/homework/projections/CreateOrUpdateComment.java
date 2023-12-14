package ru.skypro.homework.projections;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CreateOrUpdateComment {

    @NotBlank(message = "Обязательное поле")
    @Size(min = 8, max = 64, message = "Количество символов от 8 до 64")
    private String text;
}
