package ru.skypro.homework.service;

import org.springframework.security.core.Authentication;
import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.projections.Comments;
import ru.skypro.homework.projections.CreateOrUpdateComment;

public interface CommentsService {
    Comments getComments(int id);
    Comments getAllComments(int adId);
    CommentDTO addComment(int id, CreateOrUpdateComment createOrUpdateComment);
    void deleteComment(int id, int commentsId, Authentication authentication);
    CommentDTO updateComment(int id, int commentsId,
                             CreateOrUpdateComment createOrUpdateComment, Authentication authentication);
}
