package ru.skypro.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.skypro.homework.model.CommentModel;
import ru.skypro.homework.model.Role;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.Comments;
import ru.skypro.homework.projections.Register;
import ru.skypro.homework.repository.CommentRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.UserServiceSecurity;

import javax.xml.stream.events.Comment;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = HomeworkApplication.class)
@Testcontainers
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CommentsControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    private UserRepo userRepository;
    @Autowired
    private CommentRepo commentRepo;
    @Autowired
    UserServiceSecurity userServiceSecurity;
    @Autowired
    private ObjectMapper objectMapper;
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

//    private void addToDb() {
//
//        userServiceSecurity.createUser(new Register(
//                "user@mail.ru",
//                "password",
//                "user name",
//                "user surname",
//                "+711111111",
//                Role.USER));
//
//        userServiceSecurity.createUser(new Register(
//                "admin@mail.ru",
//                "password",
//                "admin name",
//                "admin surname",
//                "+72222222",
//                Role.ADMIN));
//    }

//    @AfterEach
//    public void cleanUserDataBase() {
//        userRepository.deleteAll();
//    }

    @DisplayName("Получение комментария")
    @Test
//    @WithMockUser(username = "user@mail.ru")
    public void getComments_Ok() throws Exception {
        CommentModel commentModel = new CommentModel(0, LocalDateTime.now(), "test");
        List<CommentDTO> commentDTOList = new ArrayList<>();

        mockMvc.perform(get("/comments/{id}")).
                andExpect(status().isOk()).
                andExpect(jsonPath("$").isArray()).
                andExpect(jsonPath("$").isEmpty());


    }

    @DisplayName("Добавление комментария")
    @Test
    void addEmployee_test() throws Exception {
        List<CommentDTO> commentDTOList = new ArrayList<>();
        commentDTOList.add(new CommentDTO(1, 1, "text", "test"));

        mockMvc.perform(post("/comments/{id}", commentDTOList.stream().findFirst().get().getPk())

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTOList))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("test"));

    }

    @DisplayName("Удаление комментария")
    @Test
    void deleteEmployeeById_thenCheckNotContainEmployee() throws Exception {
        UserModel userModel = new UserModel(1, "test", "test", "Testt", "test", "79137027588", Role.USER);
        CommentModel commentModel = new CommentModel(1, LocalDateTime.now(), "test");
//        List<CommentDTO> commentDTOList = new ArrayList<>();
//        commentDTOList.add(CommentMapper.toCommentDTO(commentModel));
        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", commentModel.getPk()))
                .andExpect(status().isOk());

    }

    @DisplayName("Получение комментария")
    @Test
    void getComments() throws Exception {
        mockMvc.perform(get("/comments")).
                andExpect(status().isNotFound());
    }
}
