package com.japantravel.festival.repository;

import com.japantravel.festival.entity.Festival;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface FestivalRepository extends JpaRepository<Festival, Long>,
        JpaSpecificationExecutor<Festival> {

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

//  통합 검색 (D-053 · D-083). 조건은 SearchService 가 KeywordSpec 으로 만든다. 건수 · 정렬은 Pageable 이 정한다
    @Override
    @EntityGraph(attributePaths = "prefecture")
    Page<Festival> findAll(Specification<Festival> spec, Pageable pageable);
}
