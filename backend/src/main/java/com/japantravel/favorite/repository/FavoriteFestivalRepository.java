package com.japantravel.favorite.repository;

import com.japantravel.favorite.entity.FavoriteFestival;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteFestivalRepository extends JpaRepository<FavoriteFestival, Long> {

//  목록 응답이 축제의 현 이름까지 쓰므로 둘 다 한 번에 가져온다.
    @EntityGraph(attributePaths = {"festival", "festival.prefecture"})
    List<FavoriteFestival> findByUserIdOrderByIdDesc(Long userId);

    boolean existsByUserIdAndFestivalId(Long userId, Long festivalId);

    void deleteByUserIdAndFestivalId(Long userId, Long festivalId);
}
