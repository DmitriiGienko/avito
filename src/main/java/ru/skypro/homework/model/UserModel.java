package ru.skypro.homework.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private int id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @OneToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(name = "role", nullable = false)
    private Role role;

    @OneToMany(mappedBy="userModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AdModel> adModels;

    @OneToMany(mappedBy = "userModel",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<CommentModel> commentModels;


}
