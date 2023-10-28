package ru.skypro.homework.mapper;

import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.model.AdModel;

public class AdMapper {
    public static AdDTO toAdDto(AdModel adModel) {
        AdDTO adDTO = new AdDTO();
        adDTO.setPk(adModel.getPk());
        adDTO.setTitle(adModel.getTitle());
        adDTO.setPrice(adModel.getPrice());
        adDTO.setAuthor(adModel.getUserModel().getId());
        adDTO.setImage(adModel.getImage());
        return adDTO;
    }

    public static AdModel toAdModel(AdDTO adDTO) {
        AdModel adModel = new AdModel();
        adModel.setTitle(adDTO.getTitle());
        adModel.setPk(adDTO.getPk());
        adModel.setImage(adDTO.getImage());
        adModel.setPrice(adDTO.getPrice());
        return adModel;
    }
}