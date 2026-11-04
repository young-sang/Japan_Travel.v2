package com.japantravel.news.repository;

import com.japantravel.news.entity.News;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {

//  목록은 작성자를 쓰지 않는다. id 가 아니라 작성 시각으로 정렬한다 — 시드가 과거 시각으로 넣는다
    List<News> findAllByOrderByCreatedAtDescIdDesc();

    @EntityGraph(attributePaths = "author")
    Optional<News> findById(Long id);
}
