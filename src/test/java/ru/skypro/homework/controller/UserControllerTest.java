package ru.skypro.homework.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.skypro.homework.model.Role;
import ru.skypro.homework.projections.Register;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.UserServiceSecurity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    UserServiceSecurity userServiceSecurity;
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withUsername("postgres")
            .withPassword("postgres");

    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private String getAuthenticationHeader(String login, String password) {
        String encoding = Base64.getEncoder()
                .encodeToString((login + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoding;
    }

    private void addToDb() {

        userServiceSecurity.createUser(new Register(
                "user@mail.ru",
                "password",
                "user name",
                "user surname",
                "+711111111",
                Role.USER));

        userServiceSecurity.createUser(new Register(
                "admin@mail.ru",
                "password",
                "admin name",
                "admin surname",
                "+72222222",
                Role.ADMIN));
    }

    @AfterEach
    public void cleanUserDataBase() {
        userRepository.deleteAll();
    }


    @DisplayName("Получение профиля пользователя")
    @Test

    public void shouldGetUserInfo_Ok() throws Exception {
        addToDb();
        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user@mail.ru", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@mail.ru"));
    }

    @DisplayName("ошибка авторизации пользователя")
    @Test

    public void shouldNotGetUserInfo_Unauthorized() throws Exception {
        addToDb();
        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user@mail.ru", "password1")))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("ошибка - пользователь не найден")
    @Test
    @WithMockUser(username = "user2@mail.ru")
    public void shouldNotGetUserInfo_NotFound() throws Exception {
        addToDb();
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Изменение пароля пользователя")
    @Test
    void shouldSetNewPassword_Ok() throws Exception {
        addToDb();
        JSONObject newPassword = new JSONObject();
        newPassword.put("currentPassword", "password");
        newPassword.put("newPassword", "new_password");

        mockMvc.perform(post("/users/set_password")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user@mail.ru", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPassword.toString()))
                .andExpect(status().isOk());
    }

    @DisplayName("Слишком короткий пароль")
    @Test
    void shouldNotSetNewPassword_BadRequest() throws Exception {
        addToDb();
        JSONObject newPassword = new JSONObject();
        newPassword.put("currentPassword", "password");
        newPassword.put("newPassword", "123");

        mockMvc.perform(post("/users/set_password")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user@mail.ru", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPassword.toString()))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("ошибка авторизации при смене пароля")
    @Test
    void shouldNotSetNewPassword_Unauthorized() throws Exception {
        addToDb();
        JSONObject newPassword = new JSONObject();
        newPassword.put("currentPassword", "password");
        newPassword.put("newPassword", "newPassword");

        mockMvc.perform(post("/users/set_password")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user@mail.ru", "password2"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPassword.toString()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("смена пароля запрещена")
    @Test
    void shouldNotSetNewPassword_Forbidden() throws Exception {
        addToDb();
        JSONObject newPassword = new JSONObject();
        newPassword.put("currentPassword", "password222");
        newPassword.put("newPassword", "newPassword");

        mockMvc.perform(post("/users/set_password")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user@mail.ru", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPassword.toString()))
                .andExpect(status().isForbidden());
    }


    @DisplayName("Изменение данных пользователя")
    @Test
    void shouldUpdateUser_Ok() throws Exception {
        addToDb();
        JSONObject updateUser = new JSONObject();
        updateUser.put("firstName", "newName");
        updateUser.put("lastName", "newSurname");
        updateUser.put("phone", "+72222222222");

        mockMvc.perform(patch("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user@mail.ru", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateUser.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("newName"))
                .andExpect(jsonPath("$.lastName").value("newSurname"))
                .andExpect(jsonPath("$.phone").value("+72222222222"));

    }

    @DisplayName("Изменение данных - короткое имя")
    @Test
    void shouldNotUpdateUser_BadRequest() throws Exception {
        addToDb();
        JSONObject updateUser = new JSONObject();
        updateUser.put("firstName", "ne");
        updateUser.put("lastName", "newSurname");
        updateUser.put("phone", "+72222222222");

        mockMvc.perform(patch("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user@mail.ru", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateUser.toString()))
                .andExpect(status().isBadRequest());

    }


}