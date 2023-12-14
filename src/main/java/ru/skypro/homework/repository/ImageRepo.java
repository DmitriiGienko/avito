package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.model.ImageModel;
@Repository
public interface ImageRepo extends JpaRepository<ImageModel, String> {

}
