package com.japantravel.search.service;

import com.japantravel.course.dto.CourseSummaryResponse;
import com.japantravel.course.repository.CourseRepository;
import com.japantravel.destination.dto.DestinationResponse;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.festival.dto.FestivalResponse;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.search.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//  테이블이 없는 도메인이다. 각 도메인의 리포지토리에 LIKE 쿼리를 하나씩 두고 결과를 모은다 (D-053).
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

//  대상별 상한. 옛 동작과 같다
    private static final Pageable LIMIT = PageRequest.of(0, 20);

    private final DestinationRepository destinationRepository;
    private final FestivalRepository festivalRepository;
    private final CourseRepository courseRepository;

//  빈 검색어는 에러가 아니라 빈 결과다 — 입력 중인 검색창이 그대로 부른다
    public SearchResponse search(String q) {
        if (q == null || q.isBlank()) {
            return SearchResponse.empty();
        }
        String keyword = q.trim();
        return new SearchResponse(
                destinationRepository.search(keyword, LIMIT).stream().map(DestinationResponse::from).toList(),
                festivalRepository.search(keyword, LIMIT).stream().map(FestivalResponse::from).toList(),
                courseRepository.searchPublic(keyword, LIMIT).stream().map(CourseSummaryResponse::from).toList()
        );
    }
}
