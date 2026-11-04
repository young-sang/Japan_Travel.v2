package com.japantravel.destination.repository;

import com.japantravel.destination.entity.Destination;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

//  Top25 순위 (D-074 · D-076). 점수 = 본 사람 수 + 즐겨찾기 × 2 + 리뷰 × 3, 0점은 뺀다.
//  동점이면 평균 평점 높은 순(리뷰 없는 곳은 뒤), 그다음 id 오름차순
    @Query(value = """
            select d.id as id, coalesce(h.n, 0) as viewerCount, coalesce(f.n, 0) as favoriteCount,
                   coalesce(h.n, 0) + coalesce(f.n, 0) * 2 + coalesce(r.n, 0) * 3 as score
            from destinations d
            left join (select destination_id, count(*) n from history_destinations group by destination_id) h
                   on h.destination_id = d.id
            left join (select destination_id, count(*) n from favorite_destinations group by destination_id) f
                   on f.destination_id = d.id
            left join (select destination_id, count(*) n, avg(rating) avg_rating from review_destinations
                       group by destination_id) r
                   on r.destination_id = d.id
            having score > 0
            order by score desc, r.avg_rating is null, r.avg_rating desc, d.id
            limit :limit""", nativeQuery = true)
    List<RankingRow> ranking(@Param("limit") int limit);

    interface RankingRow {
        Long getId();
        long getViewerCount();
        long getFavoriteCount();
        long getScore();
    }

    @EntityGraph(attributePaths = "prefecture")
    List<Destination> findByIdIn(Collection<Long> ids);
}
