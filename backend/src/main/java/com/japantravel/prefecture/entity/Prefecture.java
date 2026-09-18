package com.japantravel.prefecture.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prefectures")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Prefecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
