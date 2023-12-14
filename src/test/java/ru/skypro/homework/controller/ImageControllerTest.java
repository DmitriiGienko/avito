package ru.skypro.homework.controller;

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

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Import(TestPrepare.class)
class ImageControllerTest {

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


    @DisplayName("Изменение картинки объявления")
    @Test
    void shouldUpdateImage_Ok() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg", classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/ads/{id}/image", testPrepare.getAdByTitle())
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1"))
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
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg",
                classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/ads/{id}/image", testPrepare.getAdByTitle())
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("admin@mail.ru", "password"))
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
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg",
                classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/ads/{id}/image", testPrepare.getAdByTitle())
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user2@mail.ru", "password2"))
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
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg",
                classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/ads/{id}/image", testPrepare.getAdByTitle())
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
        int id = testPrepare.getAdByTitle() + 1;
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg",
                classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/ads/{id}/image", id)
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1"))
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
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg",
                classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/users/me/image")
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user1@mail.ru", "password1"))
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
        ClassPathResource classPathResource = new ClassPathResource("image-test.jpg");
        MockPart mockPart = new MockPart("image", "image-test.jpg",
                classPathResource.getInputStream().readAllBytes());
        mockPart.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
        mockMvc.perform(multipart("/users/me/image")
                        .part(mockPart)
                        .header(HttpHeaders.AUTHORIZATION,
                                testPrepare.getHeader("user5@mail.ru", "password1"))
                        .accept(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .with((request -> {
                            request.setMethod("PATCH");
                            return request;
                        })))
                .andExpect(status().isUnauthorized());
    }
}