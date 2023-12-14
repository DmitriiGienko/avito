package ru.skypro.homework.service;

import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.NewPassword;
import ru.skypro.homework.projections.UpdateUser;

import java.util.Optional;

public interface UserService {

    Optional<UserModel> findUser();

    UserDTO getUser();

    void updatePassword(NewPassword newPassword);

    UpdateUser updateUser(UpdateUser updateUser);

}
