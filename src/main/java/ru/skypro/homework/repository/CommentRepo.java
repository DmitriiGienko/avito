package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.model.AdModel;
import ru.skypro.homework.model.CommentModel;

import java.util.List;

@Repository
public interface CommentRepo extends JpaRepository<CommentModel, Integer> {
//    List<CommentModel> findByCommentId(int id);
}
