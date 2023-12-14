package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.exceptions.AccessErrorException;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.model.*;
import ru.skypro.homework.projections.Ads;
import ru.skypro.homework.projections.CreateOrUpdateAd;
import ru.skypro.homework.projections.ExtendedAd;
import ru.skypro.homework.repository.AdRepo;
import ru.skypro.homework.repository.ImageRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.AdService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.skypro.homework.mapper.AdMapper.getExtendedAd;
import static ru.skypro.homework.mapper.AdMapper.toAdDto;

@Slf4j
@Service
public class AdServiceImpl implements AdService {
    @Autowired
    AdRepo adRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    ImageRepo imageRepo;

    /**
     * Получение всех объявлений
     */

    @Override
    public Ads getAllAds() {
        List<AdDTO> adsList = adRepo.findAll().stream()
                .map(AdMapper::toAdDto)
                .collect(Collectors.toList());
        log.info("Все объявления получены");
        return new Ads(adsList.size(), adsList);

    }

    /**
     * Создание объявления
     */
    @Transactional
    public AdDTO addAd(CreateOrUpdateAd properties, MultipartFile file, Authentication authentication) {
        AdsUserDetails adsUserDetails = (AdsUserDetails) authentication.getPrincipal();

        ImageModel imageModel = new ImageModel();
        imageModel.setId(UUID.randomUUID().toString());
        try {
            imageModel.setBytes(file.getBytes());
        } catch (IOException e) {
            log.error("Ошибка добавлена картинки");
            throw new RuntimeException(e);
        }
        imageRepo.save(imageModel);

        AdModel adModel = new AdModel();
        adModel.setImage(imageModel);
        adModel.setPrice(properties.getPrice());
        adModel.setTitle(properties.getTitle());
        log.info("Добавлена картинка к объявлению {}", adModel.getTitle());
        adModel.setDescription(properties.getDescription());
        adModel.setUserModel(adsUserDetails.getUser());
        adRepo.save(adModel);
        log.info("{} добавил объявление", authentication.getName());
        return AdMapper.toAdDto(adModel);
    }

    /**
     * Получение полной информации об объявлении
     */

    @Override
    public ExtendedAd getAds(int id) {
        AdModel ad = adRepo.findById(id).orElseThrow(AdNotFoundException::new);
        log.info("Получение полной информации по объявлению {}", ad.getTitle());
        return getExtendedAd(ad);
    }

    /**
     * Внесение изменений в объявление
     */
    @Transactional
    @Override
    public AdDTO updateAd(int id, CreateOrUpdateAd createOrUpdateAdDTO, Authentication authentication) {
        AdModel adModel = adRepo.findById(id).orElseThrow(AdNotFoundException::new);
        if (!isAllowed(authentication, adModel)) {
            throw new AccessErrorException();
        }
        adModel.setTitle(createOrUpdateAdDTO.getTitle());
        adModel.setPrice(createOrUpdateAdDTO.getPrice());
        adModel.setDescription(createOrUpdateAdDTO.getDescription());
        adRepo.saveAndFlush(adModel);
        log.info("Изменение объявления {}", adModel.getTitle());
        return toAdDto(adModel);
    }

    /**
     * Удаление объявления
     */
    @Transactional
    @Override
    public void removeAd(int id, Authentication authentication) {
        AdModel adModel = adRepo.findById(id).orElseThrow(AdNotFoundException::new);

        if (!isAllowed(authentication, adModel)) {
            throw new AccessErrorException();
        }
        log.info("Объявление {} удалено", adModel.getTitle());
        adRepo.deleteById(id);
    }

    /**
     * Получение объявлений авторизированного пользователя
     */
    @Override
    public Ads getAdsMe(int userId) {
        if (userRepo.findById(userId).isEmpty()) {
            throw new UserNotFoundException();
        }
        List<AdDTO> list = adRepo.findAll().stream()
                .filter(adModel -> adModel.getUserModel().getId() == userId)
                .map(AdMapper::toAdDto)
                .collect(Collectors.toList());
        log.info("Получение объявлений авторизированного пользователя");
        return new Ads(list.size(), list);
    }


    /**
     * Проверка доступа к работе с объявлениями
     */
    public boolean isAllowed(Authentication authentication, AdModel ad) {
        UserModel user = userRepo.findByUserName(authentication.getName())
                .orElseThrow(UserNotFoundException::new);
        log.info("Проверка аутентификации {} - Ок", user.getUserName());
        return user.getId() == ad.getUserModel().getId() || user.getRole().equals(Role.ADMIN);
    }


}

