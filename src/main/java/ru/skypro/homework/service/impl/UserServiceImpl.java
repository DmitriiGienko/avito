package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.exceptions.ForbiddenException;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.NewPassword;
import ru.skypro.homework.projections.UpdateUser;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.UserService;

import java.util.Optional;


@Service
@Slf4j

public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserRepo userRepo;

    /**
     * Поиск авторизированного пользователя
     */
    public Optional<UserModel> findUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        log.info("Пользователь {} авторизован", authentication.getName());
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
        log.info("Пользователь {} есть в БД", userModel.getUserName());
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
            log.info("Пароль пользователя {} изменен", userModel.getUserName());
        } else throw new ForbiddenException();
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
        log.info("Данные пользователя {} изменены", userModel.getUserName());
        return UserMapper.mapToUpdateUser(userModel);
    }


}
