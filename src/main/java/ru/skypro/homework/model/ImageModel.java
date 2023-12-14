package ru.skypro.homework.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
/**
 * Класс сущности изображения
 */
    @Entity
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "image")
    @Data
    public class ImageModel {

        @Id
        @Column(name = "image_id")
        private String id;

        @Lob
        @Column(name = "bytes")
        @Type(type = "org.hibernate.type.BinaryType")
        private byte[] bytes;

}
