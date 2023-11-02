package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.model.Image;

public interface ImageRepo extends JpaRepository<Image, String> {
}
