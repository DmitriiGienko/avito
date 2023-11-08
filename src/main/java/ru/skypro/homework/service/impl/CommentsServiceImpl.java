package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.AdDTO;
import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.exceptions.AccessErrorException;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.CommentNotFoundException;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.AdMapper;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.model.AdModel;
import ru.skypro.homework.model.CommentModel;
import ru.skypro.homework.model.Role;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.Comments;
import ru.skypro.homework.projections.CreateOrUpdateComment;
import ru.skypro.homework.repository.AdRepo;
import ru.skypro.homework.repository.CommentRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.CommentsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static ru.skypro.homework.mapper.CommentMapper.toCommentDTO;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentsServiceImpl implements CommentsService {

    private final UserServiceImpl userService;
    private final CommentRepo commentRepo;
    private final AdRepo adRepo;
    private final UserRepo userRepo;

    /**
     * Поиск комментария
     */
    @Override
    public Comments getComments(int id) {
        CommentModel commentModel = commentRepo.findById(id).orElseThrow(CommentNotFoundException::new);
        log.info("Найден комментарий");
        return CommentMapper.toComments(commentModel);
    }


    /**
     * Получение всех комментариев по объявлению
     */
    public Comments getAllComments(int adId) {
        List<CommentDTO> commentsDTOList = commentRepo.findAll().stream()
                .filter(commentModel -> commentModel.getAdModel().getPk() == adId)
                .map(CommentMapper::toCommentDTO).collect(Collectors.toList());
        log.info("Все комментарии получены");
        return new Comments(commentsDTOList, commentsDTOList.size());
    }

    /**
     * Создание комментария
     */
    @Transactional
    @Override
    public CommentDTO addComment(int id, CreateOrUpdateComment createOrUpdateComment) {

        UserModel user = userService.findUser().orElseThrow(UserNotFoundException::new);
        AdModel adModel = adRepo.findById(id).orElseThrow(AdNotFoundException::new);

        CommentModel commentModel = new CommentModel();
        commentModel.setAdModel(adModel);
        commentModel.setText(createOrUpdateComment.getText());
        commentModel.setCreateAt(LocalDateTime.parse(LocalDateTime.now()
                .format(DateTimeFormatter.ISO_DATE_TIME)));
        commentModel.setUserModel(user);
        commentRepo.save(commentModel);
        log.info("Комментарий к объявлению {} создан", adModel.getTitle());
        return CommentMapper.toCommentDTO(commentModel);
    }

    /**
     * Удаление комментария
     */
    @Override
    public void deleteComment(int id, int commentsId, Authentication authentication) {
        CommentModel comment = getCommentFromDB(commentsId);
        if (!isAllowed(authentication, comment)) {
            throw new AccessErrorException();
        }
        log.info("Комментарий удален пользователем {}", authentication.getName());
        commentRepo.deleteById(commentsId);
    }

    /**
     * Редактирование комментария
     */
    @Override
    public CommentDTO updateComment(int id, int commentsId,
                                    CreateOrUpdateComment createOrUpdateComment, Authentication authentication) {

        CommentModel comment = getCommentFromDB(commentsId);

        if (!isAllowed(authentication, comment)) {
            throw new AccessErrorException();
        }
        comment.setText(createOrUpdateComment.getText());
        commentRepo.save(comment);
        log.info("Комментарий изменен пользователем {}", authentication.getName());
        return toCommentDTO(comment);
    }


    /**
     * Проверка доступа к работе с комментариями
     */
    public boolean isAllowed(Authentication authentication, CommentModel comment) {
        UserModel user = userRepo.findByUserName(authentication.getName())
                .orElseThrow(UserNotFoundException::new);
        log.info("Доступ к работе с комментариями разрешен для {}", authentication.getName());
        return user.getId() == comment.getUserModel().getId() || user.getRole().equals(Role.ADMIN);
    }

    /**
     * Получение комментария из базы данных
     */
    public CommentModel getCommentFromDB(int id) {
        return commentRepo.findById(id).orElseThrow(CommentNotFoundException::new);

    }
}
