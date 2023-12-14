package ru.skypro.homework.mapper;

import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.model.CommentModel;
import ru.skypro.homework.projections.Comments;
import ru.skypro.homework.projections.CreateOrUpdateComment;

import java.util.Optional;
/**
 * Класс мапинга из сущности в DTO и наоборот
 */
public class CommentMapper {
    public static CommentDTO toCommentDTO(CommentModel commentModel) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setPk(commentModel.getPk());
        commentDTO.setAuthor(commentModel.getUserModel().getId());
        Optional.ofNullable(commentModel.getUserModel().getImage())
                .ifPresent(comment -> commentDTO.setAuthorImage(
                "/image/" + commentModel.getUserModel().getImage().getId()));
        commentDTO.setAuthorFirstName(commentModel.getUserModel().getFirstName());
        commentDTO.setCreatedAt(commentModel.getCreateAt());
        commentDTO.setText(commentModel.getText());
        return commentDTO;
    }

    public static Comments toComments(CommentModel commentModel) {
        Comments comments = new Comments();
        comments.setCount(comments.getCount());
        comments.setResults(comments.getResults());
        return comments;
    }
}
