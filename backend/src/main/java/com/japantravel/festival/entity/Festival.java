package com.japantravel.festival.entity;

import com.japantravel.prefecture.entity.Prefecture;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "festivals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Festival {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

//  개최 월 1~12. D-022 에서 테이블을 나눈 이득으로 NOT NULL 을 걸었다.
    private Integer month;

//  "7월 중순" 같은 자유 텍스트. 축제는 매년 날짜가 바뀌어 DATE 로 두지 않는다.
    @Column(name = "date_text")
    private String dateText;

    private String description;

    private Double lat;

    private Double lng;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prefecture_id")
    private Prefecture prefecture;
}
