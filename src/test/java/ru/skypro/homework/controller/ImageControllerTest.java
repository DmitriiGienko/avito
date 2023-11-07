package ru.skypro.homework.controller;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ImageControllerTest {

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


    @DisplayName("Изменение картинки объявления")
    @Test
    void shouldUpdateImage_Ok() throws Exception {
        addToDb();
        int id = adRepository.findAdByTitle("Title1").get().getPk();
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg", classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/ads/{id}/image", id)
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1"))
                        .accept(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with((request -> {
                            request.setMethod("PATCH");
                            return request;
                        })))
                .andExpect(status().isOk());
    }

    @DisplayName("Изменение картинки объявления администратором")
    @Test
    void shouldUpdateImageByAdmin_Ok() throws Exception {
        addToDb();
        int id = adRepository.findAdByTitle("Title1").get().getPk();
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg", classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/ads/{id}/image", id)
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("admin@mail.ru", "password"))
                        .accept(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with((request -> {
                            request.setMethod("PATCH");
                            return request;
                        })))
                .andExpect(status().isOk());
    }

    @DisplayName("Попытка изменения объявления другим пользователем")
    @Test
    void shouldNotUpdateImageByOtherUser_isForbidden() throws Exception {
        addToDb();
        int id = adRepository.findAdByTitle("Title1").get().getPk();
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg", classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/ads/{id}/image", id)
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user2@mail.ru", "password2"))
                        .accept(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with((request -> {
                            request.setMethod("PATCH");
                            return request;
                        })))
                .andExpect(status().isForbidden())
                .andExpect(content().string("У Вас нет прав на изменение объявления!"));
    }

    @DisplayName("Пользователь не аутентифицирован")
    @Test
    void shouldNotUpdateImageByNotUser_isUnauthorized() throws Exception {
        addToDb();
        int id = adRepository.findAdByTitle("Title1").get().getPk();
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg", classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/ads/{id}/image", id)
                        .part(mockPart)
                        .accept(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with((request -> {
                            request.setMethod("PATCH");
                            return request;
                        })))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Объявление отсутствует")
    @Test
    void shouldNotUpdateImage_notFound() throws Exception {
        addToDb();
        int id = adRepository.findAdByTitle("Title1").get().getPk() + 1;
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg", classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/ads/{id}/image", id)
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1"))
                        .accept(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with((request -> {
                            request.setMethod("PATCH");
                            return request;
                        })))
                .andExpect(status().isNotFound());
    }


    @DisplayName("Обновление аватарки пользователя")
    @Test
    void shouldUpdateUserImage_Ok() throws Exception {
        addToDb();
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg", classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/users/me/image")
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user1@mail.ru", "password1"))
                        .accept(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with((request -> {
                            request.setMethod("PATCH");
                            return request;
                        })))
                .andExpect(status().isOk());
    }

    @DisplayName("Обновление аватарки не аутентифицированного пользователя")
    @Test
    void shouldNotUpdateUserImage_Unauthorized() throws Exception {
        addToDb();
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg", classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/users/me/image")
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                getAuthenticationHeader("user5@mail.ru", "password1"))
                        .accept(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with((request -> {
                            request.setMethod("PATCH");
                            return request;
                        })))
                .andExpect(status().isUnauthorized());
    }
}