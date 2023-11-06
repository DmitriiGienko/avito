package ru.skypro.homework.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.skypro.homework.model.AdModel;
import ru.skypro.homework.model.ImageModel;
import ru.skypro.homework.model.Role;
import ru.skypro.homework.projections.Register;
import ru.skypro.homework.repository.AdRepo;
import ru.skypro.homework.repository.ImageRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.UserServiceSecurity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class AdControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    AdRepo adRepository;

    @Autowired
    ImageRepo imageRepository;

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
    }

    @AfterEach
    public void cleanUserDataBase() {
        adRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("Получение всех объявлений")
    @Test
    void shouldGetAllAds_Ok() throws Exception {
        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));

        addToDb();

        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(1));
    }

    @DisplayName("Добавление объявления")
    @Test
    void shouldAddNewAd_Ok() throws Exception {

        addToDb();
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

        int id = userRepository.findByUserName("user1@mail.ru").get().getId();

        mockMvc.perform(multipart("/ads")
                        .part(mockPart, properties)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").isNumber())
                .andExpect(jsonPath("$.author").value(id))
                .andExpect(jsonPath("$.price").value(500))
                .andExpect(jsonPath("$.title").value("New ad title"));
    }

    @DisplayName("Добавление объявления с некорректными данными")
    @Test
    void shouldNotAddNewAd_BadRequest() throws Exception {

        addToDb();
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

        int id = userRepository.findByUserName("user1@mail.ru").get().getId();

        mockMvc.perform(multipart("/ads")
                        .part(mockPart, properties)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Получение полной информации по объявлению")
    @Test
    void shouldGetAdsFullInfo_Ok() throws Exception {

        addToDb();

        mockMvc.perform(get("/ads/{id}",
                        adRepository.findAdByTitle("Title1").get().getPk())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorFirstName").value("user1 name"))
                .andExpect(jsonPath("$.price").value(100));
    }

    @DisplayName("Не найдено объявление при получении полной информации")
    @Test
    void shouldNotGetAds_NotFound() throws Exception {

        addToDb();

        mockMvc.perform(get("/ads/{id}",
                        (adRepository.findAdByTitle("Title1").get().getPk()) + 1)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not found"));
    }

    @DisplayName("Ошибка аутентификации при получении полной информации")
    @Test
    void shouldNotGetAdsFullInfo_Unauthorized() throws Exception {

        addToDb();

        mockMvc.perform(get("/ads/{id}",
                        (adRepository.findAdByTitle("Title1").get().getPk()))
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1111")))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Изменение объявления владельцем")
    @Test
    void shouldUpdateAd_Ok() throws Exception {

        addToDb();

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "Title1");
        createOrUpdateAdDTO.put("price", "200");
        createOrUpdateAdDTO.put("description", "Description1");

        mockMvc.perform(patch("/ads/{id}",
                        adRepository.findAdByTitle("Title1").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrUpdateAdDTO.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(200));
    }

    @DisplayName("Изменение объявления администратором")
    @Test
    void shouldUpdateAdByAdmin_Ok() throws Exception {

        addToDb();

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "Title1");
        createOrUpdateAdDTO.put("price", "200");
        createOrUpdateAdDTO.put("description", "Description1");

        mockMvc.perform(patch("/ads/{id}",
                        adRepository.findAdByTitle("Title1").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrUpdateAdDTO.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("admin@mail.ru", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(200));
    }

    @DisplayName("Попытка изменения объявления другим пользователем")
    @Test
    void shouldNotUpdateAdByOtherUser_Forbidden() throws Exception {

        addToDb();

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "Title1");
        createOrUpdateAdDTO.put("price", "200");
        createOrUpdateAdDTO.put("description", "Description1");

        mockMvc.perform(patch("/ads/{id}",
                        adRepository.findAdByTitle("Title1").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrUpdateAdDTO.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user2@mail.ru", "password2")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("У Вас нет прав на изменение объявления!"));
    }

    @DisplayName("Попытка изменения объявления незарегистрированным пользователем")
    @Test
    void shouldNotUpdateAdByNotUser_Unauthorized() throws Exception {

        addToDb();

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "Title1");
        createOrUpdateAdDTO.put("price", "200");
        createOrUpdateAdDTO.put("description", "Description1");

        mockMvc.perform(patch("/ads/{id}",
                        adRepository.findAdByTitle("Title1").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrUpdateAdDTO.toString()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Попытка ввода некорректных данных")
    @Test
    void shouldNotUpdateAdByUser_IncorrectInputs() throws Exception {

        addToDb();

        JSONObject createOrUpdateAdDTO = new JSONObject();
        createOrUpdateAdDTO.put("title", "Title1");
        createOrUpdateAdDTO.put("price", "20000000");
        createOrUpdateAdDTO.put("description", "Description1");

        mockMvc.perform(patch("/ads/{id}",
                        adRepository.findAdByTitle("Title1").get().getPk())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrUpdateAdDTO.toString())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Удаление объявлением владельцем")
    @Test
    void shouldRemoveAd_Ok() throws Exception {

        addToDb();

        mockMvc.perform(delete("/ads/{id}",
                        adRepository.findAdByTitle("Title1").get().getPk())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk());
    }

    @DisplayName("Удаление объявлением администратором")
    @Test
    void shouldRemoveAdByAdmin_Ok() throws Exception {

        addToDb();

        mockMvc.perform(delete("/ads/{id}",
                        adRepository.findAdByTitle("Title1").get().getPk())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isOk());
    }

    @DisplayName("Попытка удаления объявления другим пользователем")
    @Test
    void shouldNotRemoveAdByOtherUser_Forbidden() throws Exception {

        addToDb();

        mockMvc.perform(delete("/ads/{id}",
                        adRepository.findAdByTitle("Title1").get().getPk())
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user2@mail.ru", "password2")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("У Вас нет прав на изменение объявления!"));
    }

    @DisplayName("Попытка удаления объявления незарегистрированным пользователем")
    @Test
    void shouldNotRemoveAdByNotUser_Unauthorized() throws Exception {

        addToDb();

        mockMvc.perform(delete("/ads/{id}",
                        adRepository.findAdByTitle("Title1").get().getPk()))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Попытка удаления несуществующего объявления")
    @Test
    void shouldNotRemoveAdBytUser_NotFound() throws Exception {

        addToDb();

        mockMvc.perform(delete("/ads/{id}",
                        (adRepository.findAdByTitle("Title1").get().getPk()) + 1)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Получение объявлений авторизованного пользователя")
    @Test
    void shouldGetAdsMe_Ok() throws Exception {

        addToDb();

        mockMvc.perform(get("/ads/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1")))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(1));

        mockMvc.perform(get("/ads/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user2@mail.ru", "password2")))
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results.length()").value(0));
    }

    @DisplayName("Получение объявлений не авторизованного пользователя")
    @Test
    void shouldNotGetAdsMe_isUnauthorized() throws Exception {

        addToDb();

        mockMvc.perform(get("/ads/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user3@mail.ru", "password1")))
                .andExpect(status().isUnauthorized());
    }
}