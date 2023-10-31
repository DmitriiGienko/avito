package ru.skypro.homework.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.model.AdModel;
import ru.skypro.homework.projections.Ads;
import ru.skypro.homework.projections.CreateOrUpdateAd;
import ru.skypro.homework.projections.ExtendedAd;
import ru.skypro.homework.repository.AdRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.AdService;

import java.util.List;
import java.util.stream.Collectors;

import static ru.skypro.homework.mapper.AdMapper.toAdDto;

@Service
public class AdServiceImpl implements AdService {
    @Autowired
    AdRepo adRepo;
    @Autowired
    UserRepo userRepo;

    /**
     * получение всех объявлений
     */

    @Override
    public Ads getAllAds() {
        List<AdDTO> adsList = adRepo.findAll().stream()
                .map(AdMapper::toAdDto)
                .collect(Collectors.toList());
        return new Ads(adsList.size(), adsList);
    }

    /**
     * создание объявления
     */
    public AdDTO addAd(CreateOrUpdateAd createOrUpdateAd, MultipartFile multipartFile, String user) {
        AdModel adModel = new AdModel();
        adModel.setImage("Test-path"); // потом удалить
//        adModel.setImage(pathImage);
        adModel.setPrice(createOrUpdateAd.getPrice());
        adModel.setTitle(createOrUpdateAd.getTitle());
        adModel.setDescription(createOrUpdateAd.getDescription());
        adModel.setUserModel(userRepo.findByUserName(user)
                .orElseThrow(() -> new UsernameNotFoundException("Not found")));
        adRepo.save(adModel);
        return toAdDto(adModel);
    }

    /**
     * получение полной информации об объявлении
     */

    @Override
    public ExtendedAd getAds(int id) {
        return userRepo.getExtendedAd(id).orElseThrow(AdNotFoundException::new);
    }

    /**
     * внесение изменений в объявление
     */
    @Transactional
    @Override
    public AdDTO updateAd(int id, CreateOrUpdateAd createOrUpdateAdDTO) {
        AdModel adModel = adRepo.findById(id).orElseThrow(AdNotFoundException::new);
        adModel.setTitle(createOrUpdateAdDTO.getTitle());
        adModel.setPrice(createOrUpdateAdDTO.getPrice());
        adModel.setDescription(createOrUpdateAdDTO.getDescription());
        adRepo.saveAndFlush(adModel);
        return toAdDto(adModel);

    }

    /**
     * удаление объявления
     */
    @Transactional
    @Override
    public void removeAd(int id) {
        if (adRepo.findById(id).isEmpty()) {
            throw new AdNotFoundException();
        }
        adRepo.deleteById(id);
    }

    /**
     * получение объявлений авторизированного пользователя
     */
    @Override
    public Ads getAdsMe(int userId) {
        List<AdDTO> list = adRepo.findAll().stream()
                .filter(adModel -> adModel.getUserModel().getId() == userId)
                .map(AdMapper::toAdDto)
                .collect(Collectors.toList());
        return new Ads(list.size(), list);
    }

    /**
     * изменение картинки объявления
     */
    @Override
    public String updateImage(int id, String pathImage) {
        return null;
    }


}

