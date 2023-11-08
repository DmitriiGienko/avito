package ru.skypro.homework.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.skypro.homework.TestPrepare;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Import(TestPrepare.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TestPrepare testPrepare;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withUsername("postgres")
            .withPassword("postgres");

    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void CreateDb() throws IOException {
        testPrepare.addToDb();
    }

    @AfterEach
    public void cleanUserDataBase() {
        testPrepare.cleanDataBase();
    }

    @DisplayName("Получение профиля пользователя")
    @Test

    public void shouldGetUserInfo_Ok() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user1@mail.ru"));
    }

    @DisplayName("ошибка авторизации пользователя")
    @Test

    public void shouldNotGetUserInfo_Unauthorized() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user@mail.ru", "password1")))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("ошибка - пользователь не найден")
    @Test
    @WithMockUser(username = "user3@mail.ru")
    public void shouldNotGetUserInfo_NotFound() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Изменение пароля пользователя")
    @Test
    void shouldSetNewPassword_Ok() throws Exception {
        JSONObject newPassword = new JSONObject();
        newPassword.put("currentPassword", "password1");
        newPassword.put("newPassword", "new_password");

        mockMvc.perform(post("/users/set_password")
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPassword.toString()))
                .andExpect(status().isOk());
    }

    @DisplayName("Слишком короткий пароль")
    @Test
    void shouldNotSetNewPassword_BadRequest() throws Exception {
        JSONObject newPassword = new JSONObject();
        newPassword.put("currentPassword", "password1");
        newPassword.put("newPassword", "123");

        mockMvc.perform(post("/users/set_password")
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPassword.toString()))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("ошибка авторизации при смене пароля")
    @Test
    void shouldNotSetNewPassword_Unauthorized() throws Exception {
        JSONObject newPassword = new JSONObject();
        newPassword.put("currentPassword", "password1");
        newPassword.put("newPassword", "newPassword");

        mockMvc.perform(post("/users/set_password")
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user@mail1.ru", "password2"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPassword.toString()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("смена пароля запрещена")
    @Test
    void shouldNotSetNewPassword_Forbidden() throws Exception {
        JSONObject newPassword = new JSONObject();
        newPassword.put("currentPassword", "password222");
        newPassword.put("newPassword", "newPassword");

        mockMvc.perform(post("/users/set_password")
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPassword.toString()))
                .andExpect(status().isForbidden());    }


    @DisplayName("Изменение данных пользователя")
    @Test
    void shouldUpdateUser_Ok() throws Exception {
        JSONObject updateUser = new JSONObject();
        updateUser.put("firstName", "newName");
        updateUser.put("lastName", "newSurname");
        updateUser.put("phone", "+72222222222");

        mockMvc.perform(patch("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1"))
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
        JSONObject updateUser = new JSONObject();
        updateUser.put("firstName", "ne");
        updateUser.put("lastName", "newSurname");
        updateUser.put("phone", "+72222222222");

        mockMvc.perform(patch("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateUser.toString()))
                .andExpect(status().isBadRequest());
    }
}