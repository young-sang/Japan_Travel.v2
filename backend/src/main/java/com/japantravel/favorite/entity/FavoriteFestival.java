package com.japantravel.favorite.entity;

import com.japantravel.festival.entity.Festival;
import com.japantravel.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//  (user_id, festival_id) 는 UNIQUE — 같은 대상을 두 번 넣지 못한다 (D-036).
@Entity
@Table(name = "favorite_festivals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FavoriteFestival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public FavoriteFestival(User user, Festival festival) {
        this.user = user;
        this.festival = festival;
        this.createdAt = LocalDateTime.now();
    }
}
