package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.exceptions.AccessErrorException;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.ImageNotFoundException;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.model.AdModel;
import ru.skypro.homework.model.ImageModel;
import ru.skypro.homework.model.Role;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.repository.AdRepo;
import ru.skypro.homework.repository.ImageRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.ImageService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {
    @Autowired
    ImageRepo imageRepo;
    @Autowired
    AdRepo adRepo;
    @Autowired
    UserRepo userRepo;

    /**
     * Отображение картинок на странице
     */
    public ResponseEntity<?> getImage(String id) {
        ImageModel image = imageRepo.findById(id).orElseThrow(ImageNotFoundException::new);
        assert image != null;
        log.info("Картинка найдена в репозитории");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(new InputStreamResource(new ByteArrayInputStream(image.getBytes())));

    }

    /**
     * Изменение картинки объявления
     */
    @Transactional
    @Override
    public String updateImage(int id, MultipartFile file, Authentication authentication) {
        AdModel ad = adRepo.findById(id).orElseThrow(AdNotFoundException::new);

        if (!isAllowed(authentication, ad)) {
            throw new AccessErrorException();
        }

        ImageModel imageModel = imageRepo.findById(ad.getImage().getId()).orElseThrow(ImageNotFoundException::new);
        try {
            imageModel.setBytes(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Картинка объявления {} изменена", imageModel.getId());
        imageRepo.saveAndFlush(imageModel);
        return ("/image/" + imageModel.getId());
    }

    /**
     * Обновление аватара пользователя
     */
    @Transactional
    @Override
    public String updateImage(MultipartFile file, Authentication authentication) {
        UserModel userModel = userRepo.findByUserName(authentication.getName()).orElseThrow(UserNotFoundException::new);
        ImageModel imageModel;
        if (!Objects.isNull(userModel.getImage())) {
            imageModel = imageRepo.findById(userModel.getImage().getId()).orElse(new ImageModel());
        } else {
            imageModel = new ImageModel();
            imageModel.setId(UUID.randomUUID().toString());
        }
        try {
            byte[] imageBytes = file.getBytes();
            imageModel.setBytes(imageBytes);

        } catch (IOException e) {
            throw new RuntimeException();
        }
        imageRepo.saveAndFlush(imageModel);
        userModel.setImage(imageModel);
        userRepo.save(userModel);
        log.info("Аватарка пользователя {} изменена", authentication.getName());
        return ("/image/" + imageModel.getId());
    }

    /**
     * Проверка доступа к работе с картинками
     */
    public boolean isAllowed(Authentication authentication, AdModel adModel) {
        UserModel user = userRepo.findByUserName(authentication.getName())
                .orElseThrow(UserNotFoundException::new);
        log.info("Доступ разрешен к работе с объявлениям для пользователя {}", authentication.getName());
        return user.getId() == adModel.getUserModel().getId() || user.getRole().equals(Role.ADMIN);
    }

}
