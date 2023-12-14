package ru.skypro.homework.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
/**
 * Класс сущности объявления
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "ad")

public class AdModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ad_id")
    private int pk;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "image_id")
    private ImageModel image;

    @Column(name = "price")
    private int price;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserModel userModel;

    @OneToMany(mappedBy = "adModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommentModel> commentModels;

}
