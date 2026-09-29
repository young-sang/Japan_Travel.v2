package com.japantravel.course.entity;

import com.japantravel.prefecture.entity.Prefecture;
import com.japantravel.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//  owner 가 null 이면 기본 제공 코스 (D-041). 기본 제공 코스는 API 로 아무도 고칠 수 없다.
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prefecture_id")
    private Prefecture prefecture;

    @Column(name = "image_path")
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

//  수정하기 전에는 null
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

//  정류장은 코스에 딸린 값이다 — 코스를 통해서만 만들고 지운다 (cascade + orphanRemoval).
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNo ASC, seq ASC")
    private List<CourseStop> stops = new ArrayList<>();

    public Course(String title, String description, Prefecture prefecture, String imagePath,
                  User owner, boolean isPublic) {
        this.title = title;
        this.description = description;
        this.prefecture = prefecture;
        this.imagePath = imagePath;
        this.owner = owner;
        this.isPublic = isPublic;
        this.createdAt = LocalDateTime.now();
    }

//  save 를 다시 부르지 않는다 — 트랜잭션 안의 변경 감지가 UPDATE 를 낸다.
//  정류장 교체는 서비스가 clearStops → flush → addStop 순서로 한다 (CourseService.replaceStops 참조).
    public void update(String title, String description, Prefecture prefecture, String imagePath, boolean isPublic) {
        this.title = title;
        this.description = description;
        this.prefecture = prefecture;
        this.imagePath = imagePath;
        this.isPublic = isPublic;
        this.updatedAt = LocalDateTime.now();
    }

    public void addStop(CourseStop stop) {
        stops.add(stop);
    }

    public void clearStops() {
        stops.clear();
    }

    public boolean isOwnedBy(Long userId) {
        return owner != null && owner.getId().equals(userId);
    }

//  공개 코스는 누구나, 비공개 코스는 작성자만 (D-041). 비로그인이면 userId 가 null.
    public boolean isVisibleTo(Long userId) {
        return isPublic || isOwnedBy(userId);
    }
}
