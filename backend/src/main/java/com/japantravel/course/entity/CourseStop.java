package com.japantravel.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 스키마의 주인은 schema.sql 이다. 필드는 schema.sql 의 컬럼을 보고 채운다.
// 컬럼명이 필드명과 다르면 @Column(name = "...") 을 붙인다 (예: image_path).
// 다른 테이블을 참조하면 @ManyToOne(fetch = FetchType.LAZY) + @JoinColumn 을 쓴다.
@Entity
@Table(name = "course_stops")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CourseStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
