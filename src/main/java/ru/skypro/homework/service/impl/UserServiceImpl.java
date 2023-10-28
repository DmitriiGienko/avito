package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.AdsUserDetails;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.NewPassword;
import ru.skypro.homework.projections.Register;
import ru.skypro.homework.projections.UpdateUser;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.UserService;
import ru.skypro.homework.service.until.Until;

import javax.persistence.EntityNotFoundException;
import java.util.Objects;

@Service
//@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDetailsManager userDetailsManager;
    private final AdsUserDetails adsUserDetails;
    private final PasswordEncoder encoder;
    private final UserRepo userRepo;


public class UserServiceImpl implements UserService {
//    @Autowired
//    private  UserDetailsManager userDetailsManager;

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserRepo userRepo;

    /**
     * Чтение информации о пользователе
     */
    @Override
    public UserDTO getUser(Authentication authentication) {
        AdsUserDetails adsUserDetails = (AdsUserDetails) authentication.getPrincipal();

        return UserMapper.mapToUserDTO(
                Objects.requireNonNull(userRepo
                        .findByUserName(adsUserDetails.getUser()
                                .getUserName()).orElse(null)));
    }

    /**
     * Редактирование пароля
     */
    @Override
    public void updatePassword(NewPassword newPassword) {

        var password1 = newPassword.getCurrentPassword();
        var newPassword1 = encoder.encode(newPassword.getNewPassword());
//        userDetailsManager.changePassword(password1, newPassword1);
    }

    /**
     * Обновление информации о пользователе
     */
    // change!!
    @Override
    public UpdateUser updateUser(UpdateUser updateUser, Authentication authentication) {
//        UserDTO userDTO = getUser(authentication);
//
//        userDTO.setLastName(updateUser.getLastName());
//        userDTO.setFirstName(updateUser.getFirstName());
//        userDTO.setPhone(updateUser.getPhone());
//
//
//        UserModel userModel = UserMapper.mapToUserModel(userDTO);
//        userRepo.save(userModel);

        UserModel user = Until.addUserFromRepo(authentication);

        user.setFirstName(updateUser.getFirstName());
        user.setLastName(updateUser.getLastName());
        user.setPhone(updateUser.getPhone());

        userRepo.save(user);
        return UserMapper.mapToUpdateUser(user);
    }

    /**
     * Обновление аватара  пользователя
     */
    @Override
    public String update(String image) {
        return "pathImage";
    }
}
