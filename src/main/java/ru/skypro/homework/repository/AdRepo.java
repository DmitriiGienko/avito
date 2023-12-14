package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.model.AdModel;

import java.util.Optional;

@Repository
public interface AdRepo extends JpaRepository<AdModel, Integer> {

    Optional<AdModel> findAdByTitle(String title);


}
