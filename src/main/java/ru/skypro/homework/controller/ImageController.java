package ru.skypro.homework.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.exceptions.ImageNotFoundException;
import ru.skypro.homework.model.ImageModel;
import ru.skypro.homework.repository.ImageRepo;
import ru.skypro.homework.service.impl.ImageServiceImpl;

import java.io.ByteArrayInputStream;

@RestController
@CrossOrigin(value = "http://localhost:3000")
public class ImageController {
    @Autowired
    ImageRepo imageRepo;
    @Autowired
    ImageServiceImpl imageService;

    @GetMapping("/image/{id}")
    private ResponseEntity<?> getImageById(@PathVariable String id) {

        return imageService.getImage(id);
//        ImageModel image = imageRepo.findById(id).orElseThrow(ImageNotFoundException::new);
////        assert image != null;
//        return ResponseEntity.ok()
//                .contentType(MediaType.IMAGE_JPEG)
//                .body(new InputStreamResource(new ByteArrayInputStream(image.getBytes())));

    }

}
