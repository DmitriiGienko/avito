package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.model.AdsUserDetails;
import ru.skypro.homework.model.ImageModel;
import ru.skypro.homework.projections.Ads;
import ru.skypro.homework.projections.CreateOrUpdateAd;
import ru.skypro.homework.projections.ExtendedAd;
import ru.skypro.homework.repository.ImageRepo;
import ru.skypro.homework.service.impl.AdServiceImpl;

import javax.validation.Valid;
import java.io.IOException;
import java.util.UUID;


@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
public class AdController {

    private final AdServiceImpl adService;
    private final ImageRepo imageRepo;


    //    Получение всех объявлений
    @GetMapping()
    public ResponseEntity<Ads> getAllAds() {
        return ResponseEntity.ok(adService.getAllAds());
    }

    //Добавление объявления
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdDTO> addAd(@RequestBody @Valid CreateOrUpdateAd createOrUpdateAdDTO,
                                       @RequestPart MultipartFile image,
                                       Authentication authentication) {
        AdsUserDetails adsUserDetails = (AdsUserDetails) authentication.getPrincipal();
        ImageModel imageModel = new ImageModel();
        imageModel.setId(UUID.randomUUID().toString());
        try {
            byte[] bytes = image.getBytes();
            imageModel.setBytes(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
        imageModel = imageRepo.save(imageModel);
//        return imageModel.getId();
        return ResponseEntity.ok(adService.addAd(createOrUpdateAdDTO, (MultipartFile) imageModel, adsUserDetails.getUser().getUserName()));
    }


    // Получение информации об объявлении
    @GetMapping("/{id}")
    public ResponseEntity<ExtendedAd> getAds(@PathVariable int id) {
        return ResponseEntity.ok(adService.getAds(id));
    }


    // Обновление объявления
    @PatchMapping("/{id}")
    public ResponseEntity<AdDTO> updateAds(@PathVariable int id,
                                           @Valid @RequestBody CreateOrUpdateAd createOrUpdateAdDTO) {
        return ResponseEntity.ok(adService.updateAd(id, createOrUpdateAdDTO));
    }


    // Удалить объявление
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeAd(@PathVariable int id) {
        adService.removeAd(id);
        return ResponseEntity.ok().build();
    }


    //Получение объявлений авторизованного пользователя
    @GetMapping("/me")
    public ResponseEntity<Ads> getAdsMe(Authentication authentication) {
        AdsUserDetails adsUserDetails = (AdsUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(adService.getAdsMe(adsUserDetails.getUser().getId()));
    }

    // Обновление картинки объявления
    @PatchMapping("/{id}/image")
    public ResponseEntity<String> updateImage(@PathVariable int id, @RequestBody String pathImage) {
        return ResponseEntity.ok(adService.updateImage(id, pathImage));
    }


}
