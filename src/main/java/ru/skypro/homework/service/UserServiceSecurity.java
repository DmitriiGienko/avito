package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.model.AdsUserDetails;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.Register;
import ru.skypro.homework.repository.UserRepo;

@Service
@RequiredArgsConstructor
public class UserServiceSecurity implements UserDetailsService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    /**
     * Метод получения данных пользователя для аутентификации
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        UserModel user = userRepo.findByUserName(username)
                .orElseThrow(UserNotFoundException::new);
        return new AdsUserDetails(user);
    }
    /**
     * Метод для создания пользователя
     */
    public void createUser(Register register) {
        UserModel user = new UserModel();
        user.setUserName(register.getUsername());
        user.setPassword(passwordEncoder.encode(register.getPassword()));
        user.setFirstName(register.getFirstName());
        user.setLastName(register.getLastName());
        user.setPhone(register.getPhone());
        user.setRole(register.getRole());
        userRepo.save(user);
    }
}
