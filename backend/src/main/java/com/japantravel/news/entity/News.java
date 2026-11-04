package com.japantravel.news.entity;

import com.japantravel.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//  관리자가 직접 쓰는 뉴스 (D-075 · D-079). author 는 계정이 지워지면 DB 가 NULL 로 바꾼다.
@Entity
@Table(name = "news")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String body;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "image_credit")
    private String imageCredit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id")
    private User author;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

//  수정하기 전에는 null
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public News(User author, String title, String body, String imagePath, String imageCredit) {
        this.author = author;
        this.title = title;
        this.body = body;
        this.imagePath = imagePath;
        this.imageCredit = imageCredit;
        this.createdAt = LocalDateTime.now();
    }

//  save 를 다시 부르지 않는다 — 트랜잭션 안의 변경 감지가 UPDATE 를 낸다.
    public void update(String title, String body, String imagePath, String imageCredit) {
        this.title = title;
        this.body = body;
        this.imagePath = imagePath;
        this.imageCredit = imageCredit;
        this.updatedAt = LocalDateTime.now();
    }
}
