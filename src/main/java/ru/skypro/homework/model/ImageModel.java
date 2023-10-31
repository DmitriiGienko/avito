package ru.skypro.homework.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "image")
public class ImageModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private int id;

    @Column(name = "bytes")
    private byte[] bytes;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id")
    private AdModel adModel;


}
