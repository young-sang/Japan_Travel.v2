package com.japantravel.history.repository;

import com.japantravel.history.entity.HistoryDestination;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HistoryDestinationRepository extends JpaRepository<HistoryDestination, Long> {

//  목록 응답이 장소의 이름 · 현 · 이미지를 쓰므로 둘 다 한 번에 가져온다.
    @EntityGraph(attributePaths = {"destination", "destination.prefecture"})
    List<HistoryDestination> findByUserIdOrderByVisitedAtDesc(Long userId);

    Optional<HistoryDestination> findByUserIdAndDestinationId(Long userId, Long destinationId);

//  파생 delete 는 행마다 SELECT 후 DELETE 하므로 한 문장으로 지운다
    @Modifying
    @Query("delete from HistoryDestination h where h.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
