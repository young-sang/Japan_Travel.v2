package com.japantravel.festival.repository;

import com.japantravel.festival.entity.Festival;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    @EntityGraph(attributePaths = "prefecture")
    Optional<Festival> findById(Long id);

//  필터가 2개라 조합이 4가지다. 쓰지 않는 조건에 null 을 넘기는 방식은
//  쓸 수 없다 — WHERE month = NULL 은 SQL 에서 unknown 이라 항상 0건이 된다.
    @EntityGraph(attributePaths = "prefecture")
    List<Festival> findAllByOrderByMonthAscIdDesc();

    @EntityGraph(attributePaths = "prefecture")
    List<Festival> findByPrefectureNameOrderByMonthAscIdDesc(String prefectureName);

    @EntityGraph(attributePaths = "prefecture")
    List<Festival> findByMonthOrderByMonthAscIdDesc(Integer month);

    @EntityGraph(attributePaths = "prefecture")
    List<Festival> findByPrefectureNameAndMonthOrderByMonthAscIdDesc(
            String prefectureName, Integer month);
}
