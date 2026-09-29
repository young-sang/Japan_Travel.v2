package com.japantravel.favorite.repository;

import com.japantravel.favorite.entity.FavoriteDestination;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteDestinationRepository extends JpaRepository<FavoriteDestination, Long> {

//  목록 응답이 여행지의 현 이름까지 쓰므로 둘 다 한 번에 가져온다.
    @EntityGraph(attributePaths = {"destination", "destination.prefecture"})
    List<FavoriteDestination> findByUserIdOrderByIdDesc(Long userId);

    boolean existsByUserIdAndDestinationId(Long userId, Long destinationId);

    void deleteByUserIdAndDestinationId(Long userId, Long destinationId);
}
