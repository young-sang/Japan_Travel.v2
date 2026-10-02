package com.japantravel.destination.entity;

import com.japantravel.prefecture.entity.Prefecture;
import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "destinations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Destination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

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

//  리뷰 집계 (D-057). 읽기 전용 서브쿼리라 조회할 때마다 계산된다. 리뷰가 없으면 avg 는 NULL.
    @Formula("(select avg(r.rating) from review_destinations r where r.destination_id = id)")
    private Double avgRating;

    @Formula("(select count(*) from review_destinations r where r.destination_id = id)")
    private long reviewCount;
}
