package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.ImageModel;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.NewPassword;
import ru.skypro.homework.projections.UpdateUser;
import ru.skypro.homework.repository.ImageRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.UserService;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j

public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    ImageServiceImpl imageService;
    @Autowired
    ImageRepo imageRepo;

    /**
     * Поиск авторизированного пользователя
     */
    public Optional<UserModel> findUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        log.info("Пользователь авторизован");
        return userRepo.findByUserName(currentPrincipalName);

    }

    /**
     * Сравнение пользователя авторизованного и из БД
     */
    public boolean comparisonUsers() {
        UserModel userModel = findUser().orElseThrow(UserNotFoundException::new);
        try {
            userRepo.findById(userModel.getId());
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException();
        }
        log.info("Пользователь есть в БД");
        return true;
    }

    /**
     * Чтение информации о пользователе
     */
    @Override
    public UserDTO getUser() {
        UserModel currentUser = findUser().orElseThrow(UserNotFoundException::new);
        return UserMapper.mapToUserDTO(currentUser);
    }

    /**
     * Редактирование пароля
     */
    @Override
    public void updatePassword(NewPassword newPassword) {
        UserModel userModel = findUser().orElseThrow(UserNotFoundException::new);
        boolean currentUserPassword = encoder.matches(newPassword.getCurrentPassword(), userModel.getPassword());
        if (currentUserPassword) {
            userModel.setPassword(encoder.encode(newPassword.getNewPassword()));
            userRepo.save(userModel);
        }
        log.info("Пароль изменен");
    }

    /**
     * Обновление информации о пользователе
     */

    @Override
    public UpdateUser updateUser(UpdateUser updateUser) {
        Optional<UserModel> currentUser = findUser();
        UserModel userModel = new UserModel();
        if (currentUser.isPresent()) {
            userModel = currentUser.get();
            userModel.setFirstName(updateUser.getFirstName());
            userModel.setLastName(updateUser.getLastName());
            userModel.setPhone(updateUser.getPhone());
            userRepo.save(userModel);
        }
        log.info("Данные пользователя изменены");
        return UserMapper.mapToUpdateUser(userModel);
    }

    /**
     * Обновление аватара  пользователя
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
        log.info("Аватар пользователя изменен");
        return ("/image/" + imageModel.getId());
    }
}
