package ru.skypro.homework.service;

import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.projections.Ads;

import ru.skypro.homework.projections.CreateOrUpdateAd;
import ru.skypro.homework.projections.ExtendedAd;

public interface AdService {

    Ads getAllAds();

    AdDTO addAd(CreateOrUpdateAd createOrUpdateAdDTO, MultipartFile multipartFile, String user);

    ExtendedAd getAds(int id) throws ChangeSetPersister.NotFoundException;

    AdDTO updateAd(int id, CreateOrUpdateAd createOrUpdateAdDTO);

    void removeAd( int id);

    Ads getAdsMe(int userId);

    String updateImage(int id, String pathImage);


}
