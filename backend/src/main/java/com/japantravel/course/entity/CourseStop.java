package com.japantravel.course.entity;

import com.japantravel.destination.entity.Destination;
import com.japantravel.festival.entity.Festival;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

//  destination · festival 중 정확히 하나만 채운다 (D-044). DB 에 CHECK 가 없으므로
//  생성 경로를 대상별 팩토리 둘로 나눠 한쪽만 채울 수 있게 한다.
@Entity
@Table(name = "course_stops")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CourseStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "day_no")
    private Integer dayNo;

    private Integer seq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    private String memo;

    private CourseStop(Course course, Integer dayNo, Integer seq,
                       Destination destination, Festival festival, String memo) {
        this.course = course;
        this.dayNo = dayNo;
        this.seq = seq;
        this.destination = destination;
        this.festival = festival;
        this.memo = memo;
    }

    public static CourseStop of(Course course, Integer dayNo, Integer seq, Destination destination, String memo) {
        return new CourseStop(course, dayNo, seq, destination, null, memo);
    }

    public static CourseStop of(Course course, Integer dayNo, Integer seq, Festival festival, String memo) {
        return new CourseStop(course, dayNo, seq, null, festival, memo);
    }
}
