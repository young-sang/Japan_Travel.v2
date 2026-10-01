package com.japantravel.festival.repository;

import com.japantravel.festival.entity.Festival;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

//  통합 검색 (D-053). 목록과 같은 정렬. 건수는 Pageable 이 자른다
    @EntityGraph(attributePaths = "prefecture")
    @Query("""
            select f from Festival f
            where f.name like %:q% or f.description like %:q% or f.prefecture.name like %:q%
            order by f.month asc, f.id desc""")
    List<Festival> search(@Param("q") String q, Pageable pageable);
}
