package ru.skypro.homework.projections;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
public class CreateOrUpdateAd {

    @NotBlank(message = "Обязательное поле")
    @Size(min = 4, max = 32, message = "Количество символов от 4 до 32")
    private String title;

    @NotNull(message = "Обязательное поле")
    @PositiveOrZero(message = "Цена не может быть меньше 0")
    @Max(value = 10000000, message = "Максимальная цена 10 млн")
    private int price;

    @NotBlank(message = "Обязательное поле")
    @Size(min = 8, max = 64, message = "Количество символов от 8 до 64")
    private String description;

}
