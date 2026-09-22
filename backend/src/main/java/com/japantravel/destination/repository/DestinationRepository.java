package com.japantravel.destination.repository;

import com.japantravel.destination.entity.Destination;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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


}
