package com.japantravel.prefecture.repository;

import com.japantravel.prefecture.entity.Prefecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrefectureRepository extends JpaRepository<Prefecture, Long> {

//    없는 현 이름으로 필터가 들어왔는지 가려낸다 (오타와 "아직 0건" 을 구분)
    boolean existsByName(String name);

//    요청 본문의 현 이름을 FK 로 쓸 엔티티로 바꾼다 (course)
    Optional<Prefecture> findByName(String name);
}
