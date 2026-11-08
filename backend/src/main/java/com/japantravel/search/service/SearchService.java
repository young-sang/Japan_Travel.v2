package com.japantravel.search.service;

import com.japantravel.course.dto.CourseSummaryResponse;
import com.japantravel.course.entity.Course;
import com.japantravel.course.repository.CourseRepository;
import com.japantravel.destination.dto.DestinationResponse;
import com.japantravel.destination.entity.Destination;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.festival.dto.FestivalResponse;
import com.japantravel.festival.entity.Festival;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.search.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//  테이블이 없는 도메인이다. 각 도메인의 리포지토리에 LIKE 조건으로 묻고 결과를 모은다 (D-053 · D-083).
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

//  대상별 상한. 옛 동작과 같다
    private static final int LIMIT = 20;

    private final DestinationRepository destinationRepository;
    private final FestivalRepository festivalRepository;
    private final CourseRepository courseRepository;

//  빈 검색어는 에러가 아니라 빈 결과다 — 입력 중인 검색창이 그대로 부른다
    public SearchResponse search(String q) {
        if (q == null || q.isBlank()) {
            return SearchResponse.empty();
        }
//      띄어 쓴 검색어는 낱말로 나누고, 모든 낱말이 들어간 것만 찾는다 (D-083)
        List<String> words = List.of(q.trim().split("\\s+"));
        Specification<Destination> destination = KeywordSpec.allWords(words, "name", "description", "prefecture.name");
        Specification<Festival> festival = KeywordSpec.allWords(words, "name", "description", "prefecture.name");
        Specification<Course> course = Specification.<Course>where((root, query, cb) -> cb.isTrue(root.get("isPublic")))
                .and(KeywordSpec.allWords(words, "title", "prefecture.name"));
        return new SearchResponse(
                destinationRepository.findAll(destination, PageRequest.of(0, LIMIT, Sort.by(Sort.Order.desc("id"))))
                        .map(DestinationResponse::from).toList(),
                festivalRepository.findAll(festival, PageRequest.of(0, LIMIT,
                                Sort.by(Sort.Order.asc("month"), Sort.Order.desc("id"))))
                        .map(FestivalResponse::from).toList(),
                courseRepository.findAll(course, PageRequest.of(0, LIMIT, Sort.by(Sort.Order.desc("id"))))
                        .map(CourseSummaryResponse::from).toList()
        );
    }
}
