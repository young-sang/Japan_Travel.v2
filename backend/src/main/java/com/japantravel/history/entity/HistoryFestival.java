package com.japantravel.history.entity;

import com.japantravel.festival.entity.Festival;
import com.japantravel.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//  (user_id, festival_id) 는 UNIQUE — 대상당 1행이고, 다시 보면 visitedAt 만 갱신한다 (D-052).
@Entity
@Table(name = "history_festivals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class HistoryFestival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @Column(name = "visited_at")
    private LocalDateTime visitedAt;

    public HistoryFestival(User user, Festival festival) {
        this.user = user;
        this.festival = festival;
        this.visitedAt = LocalDateTime.now();
    }

//  다시 본 경우. 변경 감지로 UPDATE 된다
    public void touch() {
        this.visitedAt = LocalDateTime.now();
    }
}
