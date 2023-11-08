package ru.skypro.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.skypro.homework.HomeworkApplication;
import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.model.*;
import ru.skypro.homework.projections.Comments;
import ru.skypro.homework.projections.Register;
import ru.skypro.homework.repository.AdRepo;
import ru.skypro.homework.repository.CommentRepo;
import ru.skypro.homework.repository.ImageRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.UserServiceSecurity;

import javax.xml.stream.events.Comment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class CommentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    AdRepo adRepository;

    @Autowired
    ImageRepo imageRepository;

    @Autowired
    CommentRepo commentRepo;
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

    private void addToDb() throws IOException {

        userServiceSecurity.createUser(new Register(
                "user1@mail.ru",
                "password1",
                "user1 name",
                "user1 surname",
                "+711111111",
                Role.USER));

        userServiceSecurity.createUser(new Register(
                "user2@mail.ru",
                "password2",
                "user2 name",
                "user2 surname",
                "+72222222222",
                Role.USER));

        userServiceSecurity.createUser(new Register(
                "admin@mail.ru",
                "password",
                "admin name",
                "admin surname",
                "+72222222",
                Role.ADMIN));

        ImageModel image = new ImageModel();
        image.setId(UUID.randomUUID().toString());
        image.setBytes(Files.readAllBytes(Paths.get("src/test/resources/ad-test.jpg")));
        imageRepository.save(image);

        AdModel adModel = new AdModel();
        adModel.setPk(1);
        adModel.setImage(image);
        adModel.setPrice(100);
        adModel.setTitle("Title1");
        adModel.setDescription("Description1");
        adModel.setUserModel(userRepository.findByUserName("user1@mail.ru").orElse(null));
        adRepository.save(adModel);

        CommentModel commentModel = new CommentModel();
        commentModel.setPk(1);
        commentModel.setCreateAt(LocalDateTime.now());
        commentModel.setText("TestTestTest");
        commentModel.setUserModel(userRepository.findByUserName("user1@mail.ru").orElseThrow(null));
        commentModel.setAdModel(adModel);
        commentRepo.save(commentModel);
    }

    @AfterEach
    public void cleanUserDataBase() {
        adRepository.deleteAll();
        userRepository.deleteAll();
        commentRepo.deleteAll();
    }

    @DisplayName("Получение комментария зарегестрированного пользователя")
    @Test
    public void getAllComments_Ok() throws Exception {

        addToDb();
        mockMvc.perform(get("/ads/{id}/comments", 1)
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
        commentRepo.deleteAll();
        mockMvc.perform(get("/ads/{id}/comments", 1)
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @DisplayName("Получение комментария незарегестрированного пользователя")
    @Test
    public void getAllComments_Unauthorized_NotRegistr() throws Exception {

        addToDb();
        mockMvc.perform(get("/ads/{id}/comments", 1)
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("user33@mail.ru", "password1")))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Добавление комментария зрегестрированным пользователем ")
    @Test
    public void addComment_Ok() throws Exception {

        addToDb();

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(post("/ads/{id}/comments", adRepository.findAdByTitle("Title1").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test2Test2Test2"));
    }

    @DisplayName("Добавление комментария незрегестрированным пользователем")
    @Test
    public void addComment_Unauthorized_NotRegistr() throws Exception {

        addToDb();

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(post("/ads/{id}/comments", adRepository.findAdByTitle("Title1").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("user11@mail.ru", "password1")))
                .andExpect(status().isUnauthorized());

    }

    @DisplayName("Добавление комментария, когда не найдено объявление")
    @Test
    public void addComment_NotFoundAd() throws Exception {

        addToDb();

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(post("/ads/{id}/comments", (adRepository.findAdByTitle("Title1").get().getPk()) + 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Добавление комментария, без аунтентификации пользователя")
    @Test
    public void addComment_Unauthorized_NotUser() throws Exception {

        addToDb();

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(post("/ads/{id}/comments", adRepository.findAdByTitle("Title1").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Добавление комментария, при неправельном заполнении текста")
    @Test
    public void addComment_ShortText() throws Exception {

        addToDb();

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2");

        mockMvc.perform(post("/ads/{id}/comments", adRepository.findAdByTitle("Title1").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Удаление комментария владельцем")
    @Test
    void deleteComment_Ok() throws Exception {
        addToDb();
        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}",
                        adRepository.findAdByTitle("Title1").get().getPk(),
                        commentRepo.findCommentsByText("TestTestTest").get().getPk())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk());
    }

    @DisplayName("Удаление комментария администратором")
    @Test
    void deleteComment_Ok_Admin() throws Exception {
        addToDb();
        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}",
                        adRepository.findAdByTitle("Title1").get().getPk(),
                        commentRepo.findCommentsByText("TestTestTest").get().getPk())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("admin@mail.ru", "password")))
                .andExpect(status().isOk());
    }

    @DisplayName("Удаление комментария другим пользователем")
    @Test
    void deleteComment_OtherUser() throws Exception {
        addToDb();
        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}",
                        adRepository.findAdByTitle("Title1").get().getPk(),
                        commentRepo.findCommentsByText("TestTestTest").get().getPk())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user2@mail.ru", "password2")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("У Вас нет прав на изменение объявления!"));
    }

    @DisplayName("Удаление комментария незрегестрированным пользователем")
    @Test
    void deleteComment_Unauthorized_NotRegistr() throws Exception {
        addToDb();
        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}",
                        adRepository.findAdByTitle("Title1").get().getPk(),
                        commentRepo.findCommentsByText("TestTestTest").get().getPk())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user3@mail.ru", "password2")))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Удаление комментария пользователем без аунтентификации")
    @Test
    void deleteComment_Unauthorized_NotUsers() throws Exception {
        addToDb();
        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}",
                        adRepository.findAdByTitle("Title1").get().getPk(),
                        commentRepo.findCommentsByText("TestTestTest").get().getPk()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Изменение комментария зарегестрированным пользователем")
    @Test
    public void changeComment_ok() throws Exception {

        addToDb();

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", adRepository.findAdByTitle("Title1").get().getPk(),
                        commentRepo.findCommentsByText("TestTestTest").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test2Test2Test2"));
    }

    @DisplayName("Изменение комментария администратором")
    @Test
    public void changeComment_Admin() throws Exception {

        addToDb();

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", adRepository.findAdByTitle("Title1").get().getPk(),
                        commentRepo.findCommentsByText("TestTestTest").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("admin@mail.ru", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test2Test2Test2"));
    }

    @DisplayName("Изменение комментария другим пользователем")
    @Test
    public void changeComment_OtherUser() throws Exception {

        addToDb();

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", adRepository.findAdByTitle("Title1").get().getPk(),
                        commentRepo.findCommentsByText("TestTestTest").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("user2@mail.ru", "password2")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("У Вас нет прав на изменение объявления!"));
    }

    @DisplayName("Изменение комментария незарегестрированным пользователем")
    @Test
    public void changeComment_Unauthorized_OtherUser_NotRegistr() throws Exception {

        addToDb();

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", adRepository.findAdByTitle("Title1").get().getPk(),
                        commentRepo.findCommentsByText("TestTestTest").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("user11@mail.ru", "password2")))
                .andExpect(status().isUnauthorized());

    }

    @DisplayName("Изменение комментария пользователем без аунтентификации")
    @Test
    public void changeComment_Unauthorized_NotAuthentication() throws Exception {

        addToDb();

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", adRepository.findAdByTitle("Title1").get().getPk(),
                        commentRepo.findCommentsByText("TestTestTest").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString()))
                .andExpect(status().isUnauthorized());

    }

    @DisplayName("Изменение комментария при неправильном заполнении поля")
    @Test
    public void changeComment_BadRequast_ShortText() throws Exception {

        addToDb();

        JSONObject comment = new JSONObject();
        comment.put("text", "Test");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", adRepository.findAdByTitle("Title1").get().getPk(),
                        commentRepo.findCommentsByText("TestTestTest").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION, getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isBadRequest());

    }

}