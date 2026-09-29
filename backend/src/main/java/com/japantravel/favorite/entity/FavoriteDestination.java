package com.japantravel.favorite.entity;

import com.japantravel.destination.entity.Destination;
import com.japantravel.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//  (user_id, destination_id) 는 UNIQUE — 같은 대상을 두 번 넣지 못한다 (D-036).
@Entity
@Table(name = "favorite_destinations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FavoriteDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public FavoriteDestination(User user, Destination destination) {
        this.user = user;
        this.destination = destination;
        this.createdAt = LocalDateTime.now();
    }
}
