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
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.skypro.homework.TestPrepare;
import ru.skypro.homework.model.Role;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Import(TestPrepare.class)
class AuthControllerTest {

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

    @DisplayName("Пользователь аутентифицировался")
    @Test
    void shouldBeLogin_Ok() throws Exception {
        JSONObject login = new JSONObject();
        login.put("username", "user1@mail.ru");
        login.put("password", "password1");
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login.toString()))
                .andExpect(status().isOk());
    }

    @DisplayName("Ошибка аутентификации - не верный пароль")
    @Test
    void shouldBeNotLogin_Unauthorized() throws Exception {
        JSONObject login = new JSONObject();
        login.put("username", "user1@mail.ru");
        login.put("password", "password9");
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login.toString()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Ошибка аутентификации - не верный логин")
    @Test
    void shouldBeNotLogin_NotFound() throws Exception {
        JSONObject login = new JSONObject();
        login.put("username", "user4@mail.ru");
        login.put("password", "password");
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login.toString()))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Ошибка аутентификации - короткий логин")
    @Test
    void shouldBeNotLogin_Bad() throws Exception {
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
        testPrepare.cleanDataBase();
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
        JSONObject register = new JSONObject();
        register.put("username", "user1@mail.ru");
        register.put("password", "password1");
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