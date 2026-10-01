package com.japantravel.history.entity;

import com.japantravel.destination.entity.Destination;
import com.japantravel.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//  (user_id, destination_id) 는 UNIQUE — 대상당 1행이고, 다시 보면 visitedAt 만 갱신한다 (D-052).
@Entity
@Table(name = "history_destinations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class HistoryDestination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @Column(name = "visited_at")
    private LocalDateTime visitedAt;

    public HistoryDestination(User user, Destination destination) {
        this.user = user;
        this.destination = destination;
        this.visitedAt = LocalDateTime.now();
    }

//  다시 본 경우. 변경 감지로 UPDATE 된다
    public void touch() {
        this.visitedAt = LocalDateTime.now();
    }
}
