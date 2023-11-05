package ru.skypro.homework.mapper;

import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.UpdateUser;

import java.util.Optional;

public class UserMapper {

    public static UserDTO mapToUserDTO(UserModel userModel) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userModel.getId());
        userDTO.setEmail(userModel.getUserName());
        userDTO.setFirstName(userModel.getFirstName());
        userDTO.setLastName(userModel.getLastName());
        userDTO.setPhone(userModel.getPhone());
        userDTO.setRole(userModel.getRole().name());
        Optional.ofNullable(userModel.getImage()).ifPresent(image -> userDTO.setImage(
                "/image/" + userModel.getImage().getId()));
        return userDTO;
    }
    public static UpdateUser mapToUpdateUser(UserModel userModel) {
        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName(userModel.getFirstName());
        updateUser.setLastName(userModel.getLastName());
        updateUser.setPhone(userModel.getPhone());
        return updateUser;
    }

}

