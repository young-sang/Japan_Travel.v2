package com.japantravel.review.entity;

import com.japantravel.destination.entity.Destination;
import com.japantravel.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//  같은 사용자가 같은 대상에 여러 개 쓸 수 있다 — UNIQUE 없음 (D-013).
@Entity
@Table(name = "review_destinations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

    private Integer rating;

    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

//  수정하기 전에는 null
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ReviewDestination(User user, Destination destination, Integer rating, String comment) {
        this.user = user;
        this.destination = destination;
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
