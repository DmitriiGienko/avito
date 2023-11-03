package ru.skypro.homework.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.ImageNotFoundException;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.AdModel;
import ru.skypro.homework.model.ImageModel;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.NewPassword;
import ru.skypro.homework.projections.UpdateUser;
import ru.skypro.homework.repository.ImageRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.UserService;

import java.io.IOException;
import java.util.Optional;


@Service
//@Transactional

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
     *
     * Поиск авторизированного пользователя
     */
    public Optional<UserModel> findUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userRepo.findByUserName(currentPrincipalName);
    }
    /**
     * Сравнение пользователя авторизованного и из репозитория
     */
    public boolean comparisonUsers(){
        UserModel userModel = findUser().orElseThrow(UserNotFoundException::new);
        try {
            userRepo.findById(userModel.getId());
        }catch (UserNotFoundException e){
            throw new UserNotFoundException();
        }
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
        return UserMapper.mapToUpdateUser(userModel);
    }

    /**
     * Обновление аватара  пользователя
     */
//    @Override
//    public void update(MultipartFile image) {
//
//        UserModel userModel = findUser().orElseThrow(UserNotFoundException::new);
//        userModel.getImage().getId();
//        imageService.updateImage(image,userModel.getImage().getId());
//
//    }
    @Override
    public String updateImage( MultipartFile file) {
        UserModel userModel = findUser().orElseThrow(UserNotFoundException::new);

        ImageModel imageModel = imageRepo.findById(userModel.getImage().getId()).orElseThrow(ImageNotFoundException::new);
        try {
            imageModel.setBytes(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        imageRepo.saveAndFlush(imageModel);
        return ("/user/" + imageModel.getId() + "/image");
    }
}
