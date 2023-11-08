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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.skypro.homework.TestPrepare;
import ru.skypro.homework.repository.AdRepo;
import ru.skypro.homework.repository.UserRepo;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Import(TestPrepare.class)
class AdControllerTest {
    @Autowired
    TestPrepare testPrepare;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    AdRepo adRepository;

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

    @DisplayName("Получение всех объявлений")
    @Test
    void shouldGetAllAds_Ok() throws Exception {
        userRepository.deleteAll();

        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));

        testPrepare.addToDb();

        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(1));
    }

    @DisplayName("Добавление объявления")
    @Test
    void shouldAddNewAd_Ok() throws Exception {

        adRepository.deleteAll();

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "New ad title");
        createOrUpdateAdDTO.put("price", "500");
        createOrUpdateAdDTO.put("description", "New ad description");

        String json = createOrUpdateAdDTO.toString();

        ClassPathResource classPathResource = new ClassPathResource("ad-test.jpg");
        MockPart mockPart = new MockPart("image", "ad-test.jpg", classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        MockPart properties = new MockPart("properties", json.getBytes());
        properties.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        mockMvc.perform(multipart("/ads")
                        .part(mockPart, properties)
                        .header(HttpHeaders.AUTHORIZATION, testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").isNumber())
                .andExpect(jsonPath("$.author").value(testPrepare.getUserInByUsername()))
                .andExpect(jsonPath("$.price").value(500))
                .andExpect(jsonPath("$.title").value("New ad title"));
    }

    @DisplayName("Добавление объявления с некорректными данными")
    @Test
    void shouldNotAddNewAd_BadRequest() throws Exception {

        adRepository.deleteAll();

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "New ad title");
        createOrUpdateAdDTO.put("price", "500");
        createOrUpdateAdDTO.put("description", "desc");

        String json = createOrUpdateAdDTO.toString();

        ClassPathResource classPathResource = new ClassPathResource("ad-test.jpg");
        MockPart mockPart = new MockPart("image", "ad-test.jpg", classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        MockPart properties = new MockPart("properties", json.getBytes());
        properties.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        mockMvc.perform(multipart("/ads")
                        .part(mockPart, properties)
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Получение полной информации по объявлению")
    @Test
    void shouldGetAdsFullInfo_Ok() throws Exception {

        mockMvc.perform(get("/ads/{id}",
                        testPrepare.getAdByTitle())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorFirstName").value("user1 name"))
                .andExpect(jsonPath("$.price").value(100));
    }

    @DisplayName("Не найдено объявление при получении полной информации")
    @Test
    void shouldNotGetAds_NotFound() throws Exception {

        mockMvc.perform(get("/ads/{id}",
                        (testPrepare.getAdByTitle() + 1))
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found"));
    }

    @DisplayName("Ошибка аутентификации при получении полной информации")
    @Test
    void shouldNotGetAdsFullInfo_Unauthorized() throws Exception {

        mockMvc.perform(get("/ads/{id}",
                        testPrepare.getAdByTitle())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1111")))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Изменение объявления владельцем")
    @Test
    void shouldUpdateAd_Ok() throws Exception {

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "Title1");
        createOrUpdateAdDTO.put("price", "200");
        createOrUpdateAdDTO.put("description", "Description1");

        mockMvc.perform(patch("/ads/{id}", testPrepare.getAdByTitle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrUpdateAdDTO.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(200));
    }

    @DisplayName("Изменение объявления администратором")
    @Test
    void shouldUpdateAdByAdmin_Ok() throws Exception {

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "Title1");
        createOrUpdateAdDTO.put("price", "200");
        createOrUpdateAdDTO.put("description", "Description1");

        mockMvc.perform(patch("/ads/{id}", testPrepare.getAdByTitle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrUpdateAdDTO.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("admin@mail.ru", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(200));
    }

    @DisplayName("Попытка изменения объявления другим пользователем")
    @Test
    void shouldNotUpdateAdByOtherUser_Forbidden() throws Exception {

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "Title1");
        createOrUpdateAdDTO.put("price", "200");
        createOrUpdateAdDTO.put("description", "Description1");

        mockMvc.perform(patch("/ads/{id}", testPrepare.getAdByTitle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrUpdateAdDTO.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user2@mail.ru", "password2")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("У Вас нет прав на изменение объявления!"));
    }

    @DisplayName("Попытка изменения объявления незарегистрированным пользователем")
    @Test
    void shouldNotUpdateAdByNotUser_Unauthorized() throws Exception {

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "Title1");
        createOrUpdateAdDTO.put("price", "200");
        createOrUpdateAdDTO.put("description", "Description1");

        mockMvc.perform(patch("/ads/{id}", testPrepare.getAdByTitle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrUpdateAdDTO.toString()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Попытка ввода некорректных данных")
    @Test
    void shouldNotUpdateAdByUser_IncorrectInputs() throws Exception {

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "Title1");
        createOrUpdateAdDTO.put("price", "20000000");
        createOrUpdateAdDTO.put("description", "Description1");

        mockMvc.perform(patch("/ads/{id}", testPrepare.getAdByTitle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrUpdateAdDTO.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Удаление объявлением владельцем")
    @Test
    void shouldRemoveAd_Ok() throws Exception {

        mockMvc.perform(delete("/ads/{id}", testPrepare.getAdByTitle())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk());
    }

    @DisplayName("Удаление объявлением администратором")
    @Test
    void shouldRemoveAdByAdmin_Ok() throws Exception {

        mockMvc.perform(delete("/ads/{id}", testPrepare.getAdByTitle())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("admin@mail.ru", "password")))
                .andExpect(status().isOk());
    }

    @DisplayName("Попытка удаления объявления другим пользователем")
    @Test
    void shouldNotRemoveAdByOtherUser_Forbidden() throws Exception {

        mockMvc.perform(delete("/ads/{id}", testPrepare.getAdByTitle())
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user2@mail.ru", "password2")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("У Вас нет прав на изменение объявления!"));
    }

    @DisplayName("Попытка удаления объявления незарегистрированным пользователем")
    @Test
    void shouldNotRemoveAdByNotUser_Unauthorized() throws Exception {

        mockMvc.perform(delete("/ads/{id}", testPrepare.getAdByTitle()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Попытка удаления несуществующего объявления")
    @Test
    void shouldNotRemoveAdBytUser_NotFound() throws Exception {

        mockMvc.perform(delete("/ads/{id}", (testPrepare.getAdByTitle() + 1))
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Получение объявлений авторизованного пользователя")
    @Test
    void shouldGetAdsMe_Ok() throws Exception {

        mockMvc.perform(get("/ads/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1")))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(1));

        mockMvc.perform(get("/ads/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user2@mail.ru", "password2")))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(0));
    }

    @DisplayName("Получение объявлений не авторизованного пользователя")
    @Test
    void shouldNotGetAdsMe_isUnauthorized() throws Exception {

        mockMvc.perform(get("/ads/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user3@mail.ru", "password1")))
                .andExpect(status().isUnauthorized());
    }
}