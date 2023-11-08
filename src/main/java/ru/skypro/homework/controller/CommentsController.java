package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.projections.Comments;
import ru.skypro.homework.projections.CreateOrUpdateComment;
import ru.skypro.homework.service.CommentsService;

import javax.validation.Valid;

@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
public class CommentsController {

    private final CommentsService commentsService;
    /**
     * Получение комментария
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<Comments> getComments(@PathVariable int id) {
        return ResponseEntity.ok(commentsService.getAllComments(id));
    }

    /**
     * Добавление комментария
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDTO> addComment(@PathVariable int id,
                                                 @Valid @RequestBody CreateOrUpdateComment createOrUpdateComment) {
        return ResponseEntity.ok(commentsService.addComment(id, createOrUpdateComment));
    }
    /**
     * Удаление комментария
     */
    @DeleteMapping("/{adId}/comments/{commentId}")
    public void deleteComment(@PathVariable int adId, @PathVariable int commentId, Authentication authentication) {
        commentsService.deleteComment(adId, commentId, authentication);
    }
    /**
     * Изменение комментария
     */
    @PatchMapping("/{adId}/comments/{commentId}")
    public CommentDTO updateComment(@PathVariable int adId, @PathVariable int commentId,
                                    @Valid @RequestBody CreateOrUpdateComment createOrUpdateComment,
                                    Authentication authentication) {
        return commentsService.updateComment(adId, commentId, createOrUpdateComment, authentication);
    }
}
