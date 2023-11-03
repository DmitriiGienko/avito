package ru.skypro.homework.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.Image;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.NewPassword;
import ru.skypro.homework.projections.UpdateUser;
import ru.skypro.homework.repository.ImageRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserService;
import ru.skypro.homework.service.util.Util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@Service
//@Transactional

public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserRepo userRepo;
    private final ImageService imageService;
    @Autowired
    private Util util;
    @Autowired
    ImageRepo imageRepo;

    public Optional<UserModel> findUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userRepo.findByUserName(currentPrincipalName);
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
    @Override
    public String updateUserImage(MultipartFile image) throws IOException {

//        public boolean patchAuthorizedUserPicture(MultipartFile image) {

//            try {
//                byte[] imageBytes = image.getBytes();
//                Image multipartToEntity = new Image();
//                multipartToEntity.setBytes(imageBytes);
//                imageRepo.save(multipartToEntity);
//                authorizedUser.setImageAvatar(multipartToEntity);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            userRepository.save(authorizedUser);

        UserModel user = findUser().orElseThrow();
        Image oldImage = user.getImage();
        if (oldImage == null) {
            Image newImage = imageService.createImage(image);
            user.setImage(newImage);
        } else {

            Image image1 = imageRepo.findById(user.getImage().getId()).orElseThrow(UserNotFoundException::new);

            image1.setBytes(image.getBytes());

//            Image updatedImage = imageService.updateImage(image, oldImage);
//            user.setImage(updatedImage);
        }
//        userRepo.save(user);
        return UserMapper.mapToUserDTO(user).getImage();

    }

}
