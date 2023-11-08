package ru.skypro.homework;

import org.springframework.beans.factory.annotation.Autowired;
import ru.skypro.homework.model.AdModel;
import ru.skypro.homework.model.CommentModel;
import ru.skypro.homework.model.ImageModel;
import ru.skypro.homework.model.Role;
import ru.skypro.homework.projections.Register;
import ru.skypro.homework.repository.AdRepo;
import ru.skypro.homework.repository.CommentRepo;
import ru.skypro.homework.repository.ImageRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.UserServiceSecurity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

public class TestPrepare {

    @Autowired
    UserServiceSecurity userServiceSecurity;

    @Autowired
    ImageRepo imageRepository;

    @Autowired
    UserRepo userRepository;

    @Autowired
    AdRepo adRepository;

    @Autowired
    CommentRepo commentRepo;


    public void addToDb() throws IOException {

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
                "+73333333333",
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

    public void cleanDataBase() {
        userRepository.deleteAll();
    }

    public int getAdByTitle() {
        return adRepository.findAdByTitle("Title1").get().getPk();
    }

    public int getUserInByUsername() {
        return userRepository.findByUserName("user1@mail.ru").get().getId();
    }

    public int getCommentIdByText() {
        return commentRepo.findCommentsByText("TestTestTest").get().getPk();
    }

    public String getHeader(String login, String password) {
        String encoding = Base64.getEncoder()
                .encodeToString((login + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoding;
    }

}
