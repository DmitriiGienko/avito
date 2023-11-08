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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.skypro.homework.TestPrepare;
import ru.skypro.homework.repository.CommentRepo;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Import(TestPrepare.class)
public class CommentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    TestPrepare testPrepare;

    @Autowired
    CommentRepo commentRepo;

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

    @DisplayName("Получение комментария зарегистрированного пользователя")
    @Test
    public void getAllComments_Ok() throws Exception {

        mockMvc.perform(get("/ads/{id}/comments", 1)
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
        commentRepo.deleteAll();
        mockMvc.perform(get("/ads/{id}/comments", 1)
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @DisplayName("Получение комментария незарегистрированного пользователя")
    @Test
    public void getAllComments_Unauthorized_NotRegistr() throws Exception {

        mockMvc.perform(get("/ads/{id}/comments", 1)
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user33@mail.ru", "password1")))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Добавление комментария зарегистрированным пользователем ")
    @Test
    public void addComment_Ok() throws Exception {

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(post("/ads/{id}/comments", testPrepare.getAdByTitle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test2Test2Test2"));
    }

    @DisplayName("Добавление комментария незарегистрированным пользователем")
    @Test
    public void addComment_Unauthorized_NotRegistr() throws Exception {

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(post("/ads/{id}/comments", testPrepare.getAdByTitle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user11@mail.ru", "password1")))
                .andExpect(status().isUnauthorized());

    }

    @DisplayName("Добавление комментария, когда не найдено объявление")
    @Test
    public void addComment_NotFoundAd() throws Exception {

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(post("/ads/{id}/comments",
                        (testPrepare.getAdByTitle() + 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Добавление комментария, без аутентификации пользователя")
    @Test
    public void addComment_Unauthorized_NotUser() throws Exception {

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(post("/ads/{id}/comments", testPrepare.getAdByTitle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Добавление комментария, при неправильном заполнении текста")
    @Test
    public void addComment_ShortText() throws Exception {

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2");

        mockMvc.perform(post("/ads/{id}/comments", testPrepare.getAdByTitle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Удаление комментария владельцем")
    @Test
    void deleteComment_Ok() throws Exception {

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}",
                        testPrepare.getAdByTitle(),
                        testPrepare.getCommentIdByText())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk());
    }

    @DisplayName("Удаление комментария администратором")
    @Test
    void deleteComment_Ok_Admin() throws Exception {

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", testPrepare.getAdByTitle(),
                        testPrepare.getCommentIdByText())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("admin@mail.ru", "password")))
                .andExpect(status().isOk());
    }

    @DisplayName("Удаление комментария другим пользователем")
    @Test
    void deleteComment_OtherUser() throws Exception {

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", testPrepare.getAdByTitle(),
                        testPrepare.getCommentIdByText())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user2@mail.ru", "password2")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("У Вас нет прав на изменение объявления!"));
    }

    @DisplayName("Удаление комментария незарегистрированным пользователем")
    @Test
    void deleteComment_Unauthorized_NotRegistr() throws Exception {

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", testPrepare.getAdByTitle(),
                        testPrepare.getCommentIdByText())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user3@mail.ru", "password2")))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Удаление комментария пользователем без аутентификации")
    @Test
    void deleteComment_Unauthorized_NotUsers() throws Exception {

        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", testPrepare.getAdByTitle(),
                        testPrepare.getCommentIdByText()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Изменение комментария зарегистрированным пользователем")
    @Test
    public void changeComment_ok() throws Exception {

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", testPrepare.getAdByTitle(),
                        testPrepare.getCommentIdByText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test2Test2Test2"));
    }

    @DisplayName("Изменение комментария администратором")
    @Test
    public void changeComment_Admin() throws Exception {

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", testPrepare.getAdByTitle(),
                        testPrepare.getCommentIdByText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("admin@mail.ru", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Test2Test2Test2"));
    }

    @DisplayName("Изменение комментария другим пользователем")
    @Test
    public void changeComment_OtherUser() throws Exception {

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", testPrepare.getAdByTitle(),
                        testPrepare.getCommentIdByText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user2@mail.ru", "password2")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("У Вас нет прав на изменение объявления!"));
    }

    @DisplayName("Изменение комментария незарегистрированным пользователем")
    @Test
    public void changeComment_Unauthorized_OtherUser_NotRegistr() throws Exception {

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", testPrepare.getAdByTitle(),
                        testPrepare.getCommentIdByText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user11@mail.ru", "password2")))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Изменение комментария пользователем без аутентификации")
    @Test
    public void changeComment_Unauthorized_NotAuthentication() throws Exception {

        JSONObject comment = new JSONObject();
        comment.put("text", "Test2Test2Test2");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", testPrepare.getAdByTitle(),
                        testPrepare.getCommentIdByText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Изменение комментария при неправильном заполнении поля")
    @Test
    public void changeComment_BadRequest_ShortText() throws Exception {

        JSONObject comment = new JSONObject();
        comment.put("text", "Test");

        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", testPrepare.getAdByTitle(),
                        testPrepare.getCommentIdByText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(comment.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isBadRequest());
    }

}