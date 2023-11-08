package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.model.AdModel;
import ru.skypro.homework.model.CommentModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepo extends JpaRepository<CommentModel, Integer> {
    Optional<CommentModel> findCommentsByText(String text);
}
