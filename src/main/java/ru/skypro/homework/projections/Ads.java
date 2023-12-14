package ru.skypro.homework.projections;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.skypro.homework.dto.AdDTO;

import java.util.List;

@AllArgsConstructor
@Data
public class Ads {
    private int count;
    private List<AdDTO> results;

}
