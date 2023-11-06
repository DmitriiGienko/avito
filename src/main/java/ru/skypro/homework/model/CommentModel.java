package ru.skypro.homework.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
/**
 * Класс сущности комментария
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Table(name = "comment")
public class CommentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private int pk;

    @Column(name = "create_data")
    private LocalDateTime createAt;

    @Column(name = "text")
    private String text;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserModel userModel;

    // появляется поле ad_id в таблице comments
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id")
    private AdModel adModel;

    public CommentModel(int pk, LocalDateTime createAt, String text) {
        this.pk = pk;
        this.createAt = createAt;
        this.text = text;
    }
}
