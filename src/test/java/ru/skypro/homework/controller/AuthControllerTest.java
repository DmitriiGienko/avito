package ru.skypro.homework.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class AuthControllerTest {

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
                "+71111111111",
                Role.USER));

        userServiceSecurity.createUser(new Register(
                "admin@mail.ru",
                "password",
                "admin name",
                "admin surname",
                "+73333333333",
                Role.ADMIN));
    }

    @AfterEach
    public void cleanUserDataBase() {
        userRepository.deleteAll();
    }


    @DisplayName("Пользователь аутентифицировался")
    @Test
    void shouldBeLogin_Ok() throws Exception {
        addToDb();
        JSONObject login = new JSONObject();
        login.put("username", "user@mail.ru");
        login.put("password", "password");
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login.toString()))
                .andExpect(status().isOk());
    }

    @DisplayName("Ошибка аутентификации - не верный пароль")
    @Test
    void shouldBeNotLogin_Unauthorized() throws Exception {
        addToDb();
        JSONObject login = new JSONObject();
        login.put("username", "user@mail.ru");
        login.put("password", "password1");
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login.toString()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Ошибка аутентификации - не верный логин")
    @Test
    void shouldBeNotLogin_NotFound() throws Exception {
        addToDb();
        JSONObject login = new JSONObject();
        login.put("username", "user2@mail.ru");
        login.put("password", "password");
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login.toString()))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Ошибка аутентификации - короткий логин")
    @Test
    void shouldBeNotLogin_Bad() throws Exception {
        addToDb();
        JSONObject login = new JSONObject();
        login.put("username", "us");
        login.put("password", "password");
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login.toString()))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Регистрация пользователя")
    @Test
    void shouldBeRegistered_Ok() throws Exception {
        JSONObject register = new JSONObject();
        register.put("username", "user@mail.ru");
        register.put("password", "password");
        register.put("firstName", "user name");
        register.put("lastName", "user surname");
        register.put("phone", "+71111111111");
        register.put("role", Role.USER);
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register.toString()))
                .andExpect(status().isCreated());
    }

    @DisplayName("Имя занято")
    @Test
    void shouldBeNotRegistered_BadRequest() throws Exception {
        addToDb();
        JSONObject register = new JSONObject();
        register.put("username", "user@mail.ru");
        register.put("password", "password");
        register.put("firstName", "user name");
        register.put("lastName", "user surname");
        register.put("phone", "+71111111111");
        register.put("role", Role.USER);
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Пользователь с таким именем уже существует"));
    }

    @DisplayName("Ошибка ввода данных")
    @Test
    void shouldBeNotRegistered_BadInputs() throws Exception {
        JSONObject register = new JSONObject();
        register.put("username", "user@mail.ru");
        register.put("password", "password");
        register.put("firstName", null);
        register.put("lastName", "user surname");
        register.put("phone", "+71111111111");
        register.put("role", Role.USER);
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register.toString()))
                .andExpect(status().isBadRequest());
    }
}