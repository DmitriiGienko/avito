package ru.skypro.homework.service;

import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.NewPassword;
import ru.skypro.homework.projections.Register;
import ru.skypro.homework.projections.UpdateUser;

import java.io.IOException;
import java.util.Optional;

public interface UserService {

    Optional<UserModel> findUser();
    UserDTO getUser();

    void updatePassword(NewPassword newPassword);

    UpdateUser updateUser(UpdateUser updateUser);

    String updateUserImage(MultipartFile image) throws IOException;
}
