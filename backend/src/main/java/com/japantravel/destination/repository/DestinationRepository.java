package com.japantravel.destination.repository;

import com.japantravel.destination.entity.Destination;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    @EntityGraph(attributePaths = "prefecture")
    Optional<Destination> findById(Long id);

//    정렬 상태로 가져오기
    @EntityGraph(attributePaths = "prefecture")
    List<Destination> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = "prefecture")
    List<Destination> findByPrefectureNameOrderByIdDesc(String prefectureName);

//  통합 검색 (D-053). 이름 · 설명 · 현 중 하나라도 포함하면 맞는다. 건수는 Pageable 이 자른다
    @EntityGraph(attributePaths = "prefecture")
    @Query("""
            select d from Destination d
            where d.name like %:q% or d.description like %:q% or d.prefecture.name like %:q%
            order by d.id desc""")
    List<Destination> search(@Param("q") String q, Pageable pageable);
}
