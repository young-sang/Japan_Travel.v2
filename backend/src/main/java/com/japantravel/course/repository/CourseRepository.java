package com.japantravel.course.repository;

import com.japantravel.course.entity.Course;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

//  정류장은 Course 의 cascade 로 저장·삭제되므로 CourseStopRepository 를 두지 않는다.
public interface CourseRepository extends JpaRepository<Course, Long> {

//  상세 — 정류장과 정류장의 원본 장소(현 포함)까지 한 번에 가져온다
    @EntityGraph(attributePaths = {
            "prefecture", "owner",
            "stops", "stops.destination.prefecture", "stops.festival.prefecture"})
    Optional<Course> findWithStopsById(Long id);

//  목록(요약)은 정류장을 조인하지 않는다 (D-046)
    @EntityGraph(attributePaths = {"prefecture", "owner"})
    List<Course> findByIsPublicTrueOrderByIdDesc();

    @EntityGraph(attributePaths = {"prefecture", "owner"})
    List<Course> findByIsPublicTrueAndPrefectureNameOrderByIdDesc(String prefectureName);

    @EntityGraph(attributePaths = {"prefecture", "owner"})
    List<Course> findByOwnerIdOrderByIdDesc(Long ownerId);

//  통합 검색 (D-053). 공개 코스만, 제목 · 현으로 찾는다. 건수는 Pageable 이 자른다
    @EntityGraph(attributePaths = {"prefecture", "owner"})
    @Query("""
            select c from Course c
            where c.isPublic = true and (c.title like %:q% or c.prefecture.name like %:q%)
            order by c.id desc""")
    List<Course> searchPublic(@Param("q") String q, Pageable pageable);
}
