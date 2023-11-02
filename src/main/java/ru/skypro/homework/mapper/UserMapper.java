package ru.skypro.homework.mapper;

import ru.skypro.homework.dto.UserDTO;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.UpdateUser;

public class UserMapper {


    public static UserDTO mapToUserDTO(UserModel userModel) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userModel.getId());
        userDTO.setEmail(userModel.getUserName());
        userDTO.setFirstName(userModel.getFirstName());
        userDTO.setLastName(userModel.getLastName());
        userDTO.setPhone(userModel.getPhone());
        userDTO.setRole(userModel.getRole().name());
//        userDTO.setImage(String.valueOf(userModel.getImage()));
        return userDTO;
    }

    public static UserModel mapToUserModel(UserDTO userDTO) {
        UserModel userModel = new UserModel();
        userModel.setId(userDTO.getId());
        userModel.setUserName(userDTO.getEmail());
        userModel.setFirstName(userDTO.getFirstName());
        userModel.setLastName(userDTO.getLastName());
        userModel.setPhone(userDTO.getPhone());
        return userModel;
    }

    public static UpdateUser mapToUpdateUser(UserModel userModel) {
        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName(userModel.getFirstName());
        updateUser.setLastName(userModel.getLastName());
        updateUser.setPhone(userModel.getPhone());
        return updateUser;

    }
    public static UpdateUser mapToUpdateUser(UserDTO userDTO) {
        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName(userDTO.getFirstName());
        updateUser.setLastName(userDTO.getLastName());
        updateUser.setPhone(userDTO.getPhone());
        return updateUser;

    }
//    public static UserModel mapToUserModel(UpdateUser u) {
//        UpdateUser updateUser = new UpdateUser();
//        updateUser.setFirstName(userDTO.getFirstName());
//        updateUser.setLastName(userDTO.getLastName());
//        updateUser.setPhone(userDTO.getPhone());
//        return updateUser;
//
//    }
}

