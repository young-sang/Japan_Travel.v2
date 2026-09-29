package com.japantravel.review.repository;

import com.japantravel.review.entity.ReviewFestival;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewFestivalRepository extends JpaRepository<ReviewFestival, Long> {

//  응답에 작성자 닉네임이 들어가므로 user 를 함께 가져온다.
    @EntityGraph(attributePaths = "user")
    List<ReviewFestival> findByFestivalIdOrderByIdDesc(Long festivalId);

//  경로의 축제에 딸린 리뷰인지까지 한 번에 확인한다. 아니면 빈 값 → 404.
    @EntityGraph(attributePaths = "user")
    Optional<ReviewFestival> findByIdAndFestivalId(Long id, Long festivalId);
}
