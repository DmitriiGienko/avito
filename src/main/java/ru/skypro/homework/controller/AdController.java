package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.model.AdsUserDetails;
import ru.skypro.homework.projections.Ads;
import ru.skypro.homework.projections.CreateOrUpdateAd;
import ru.skypro.homework.projections.ExtendedAd;
import ru.skypro.homework.service.impl.AdServiceImpl;

import javax.validation.Valid;

@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
public class AdController {

    private final AdServiceImpl adService;

    /**
     * Получение всех объявлений
     */
    @GetMapping()
    public ResponseEntity<Ads> getAllAds() {
        return ResponseEntity.ok(adService.getAllAds());
    }

    /**
     * Добавление объявления
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdDTO> addAd(@RequestPart(value = "properties") @Valid CreateOrUpdateAd properties,
                                       @RequestPart("image") MultipartFile image,
                                       Authentication authentication) {
        return ResponseEntity.ok(adService.addAd(properties, image, authentication));
    }

    /**
     * Получение информации об объявлении
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExtendedAd> getAds(@PathVariable int id) {
        return ResponseEntity.ok(adService.getAds(id));
    }

    /**
     * Обновление объявления
     */
    @PatchMapping("/{id}")
    public ResponseEntity<AdDTO> updateAds(@PathVariable int id,
                                           @Valid @RequestBody CreateOrUpdateAd createOrUpdateAdDTO,
                                           Authentication authentication) {
        return ResponseEntity.ok(adService.updateAd(id, createOrUpdateAdDTO, authentication));
    }

    /**
     * Удаление объявления
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeAd(@PathVariable int id, Authentication authentication) {
        adService.removeAd(id, authentication);
        return ResponseEntity.ok().build();
    }

    /**
     * Получение объявлений авторизованного пользователя
     */
    @GetMapping("/me")
    public ResponseEntity<Ads> getAdsMe(Authentication authentication) {
        AdsUserDetails adsUserDetails = (AdsUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(adService.getAdsMe(adsUserDetails.getUser().getId()));
    }

}
