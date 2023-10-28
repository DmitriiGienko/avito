package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.exceptions.AdNotFoundException;
import ru.skypro.homework.exceptions.CommentNotFoundException;
import ru.skypro.homework.exceptions.UserNotFoundException;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.model.AdModel;
import ru.skypro.homework.model.CommentModel;
import ru.skypro.homework.model.UserModel;
import ru.skypro.homework.projections.Comments;
import ru.skypro.homework.projections.CreateOrUpdateComment;
import ru.skypro.homework.repository.AdRepo;
import ru.skypro.homework.repository.CommentRepo;
import ru.skypro.homework.repository.UserRepo;
import ru.skypro.homework.service.CommentsService;
import ru.skypro.homework.service.until.Until;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
//@Transactional
@RequiredArgsConstructor
public class CommentsServiceImpl implements CommentsService {
    private final UserServiceImpl userService;
    private final CommentRepo commentRepo;
    private final AdServiceImpl adService;
    private final AdRepo adRepo;
    private final UserRepo userRepo;


    /**
     * Поиск комментария
     */
    @Override
    public Comments getComments(int id) {
        List<CommentDTO> comments = commentRepo.findById(id)
                .stream()
                .map(CommentMapper::toCommentDTO)
                .collect(Collectors.toList());
        if (comments.isEmpty())
            throw new CommentNotFoundException();
        else
            return CommentMapper.toComments((CommentModel) comments);
    }
    @Override
    public Comments getAllComments ( int adId){
        adService.getAds(adId);
        List<CommentDTO> commentsDTOList = commentRepo.findAll().stream().map(CommentMapper::toCommentDTO).collect(Collectors.toList());
        return new Comments(commentsDTOList, commentsDTOList.size());
    }


    /**
     * Создание комментария
     */
    @Override
    public CommentDTO addComment(int id, CreateOrUpdateComment createOrUpdateComment) {
        UserModel user = userService.findUser().get();
        AdModel adModel = adRepo.findById(id).orElseThrow();

        CommentModel commentModel = new CommentModel();
        commentModel.setAdModel(adModel);
        commentModel.setText(createOrUpdateComment.getText());
        commentModel.setCreateAt(LocalDateTime.parse(LocalDateTime.now()
                .format(DateTimeFormatter.ISO_DATE_TIME)));
        commentModel.setUserModel(user);
        commentRepo.save(commentModel);
        return CommentMapper.toCommentDTO(commentModel);
    }

    /**
     * Удаление коментария
     */
    @Override
    public void deleteComment(int adId, int commentsId) {
        if (commentsId > getComments(adId).getResults().size()
                || commentsId <= 0) {
            throw new CommentNotFoundException();
        }
        commentRepo.deleteById(commentsId);
    }

    /**
     * Редактирование комментария
     */
    @Override
    public Comments updateComment(int adId, int commentsId, CreateOrUpdateComment createOrUpdateComment) {
        if (commentsId > getComments(adId).getResults().size() || commentsId <= 0) {
            throw new CommentNotFoundException();
        }
        CommentModel updateComment=commentRepo.findById(commentsId).orElseThrow();
        updateComment.setText(createOrUpdateComment.getText());
        commentRepo.save(updateComment);
        return CommentMapper.toComments(updateComment);
        }
    }

