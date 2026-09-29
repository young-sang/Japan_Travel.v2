package com.japantravel.review.entity;

import com.japantravel.festival.entity.Festival;
import com.japantravel.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//  같은 사용자가 같은 대상에 여러 개 쓸 수 있다 — UNIQUE 없음 (D-013).
@Entity
@Table(name = "review_festivals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewFestival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    private Integer rating;

    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

//  수정하기 전에는 null
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ReviewFestival(User user, Festival festival, Integer rating, String comment) {
        this.user = user;
        this.festival = festival;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

//  save 를 다시 부르지 않는다 — 트랜잭션 안의 변경 감지가 UPDATE 를 낸다.
    public void update(Integer rating, String comment) {
        this.rating = rating;
        this.comment = comment;
        this.updatedAt = LocalDateTime.now();
    }
}
