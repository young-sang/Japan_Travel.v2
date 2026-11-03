package com.japantravel.search;

import com.japantravel.course.repository.CourseRepository;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.search.dto.SearchResponse;
import com.japantravel.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock DestinationRepository destinationRepository;
    @Mock FestivalRepository festivalRepository;
    @Mock CourseRepository courseRepository;

    @InjectMocks SearchService searchService;

    @Test
    void 빈_검색어는_쿼리_없이_빈_결과() {
        SearchResponse res = searchService.search("  ");

        assertThat(res.destinations()).isEmpty();
        assertThat(res.festivals()).isEmpty();
        assertThat(res.courses()).isEmpty();
        verifyNoInteractions(destinationRepository, festivalRepository, courseRepository);
    }

    @Test
    void 검색어는_앞뒤_공백을_떼고_대상별_20건으로_자른다() {
        searchService.search("  교토 ");

        verify(destinationRepository).search(eq("교토"), argThat((Pageable p) -> p.getPageSize() == 20));
        verify(festivalRepository).search(eq("교토"), argThat((Pageable p) -> p.getPageSize() == 20));
        verify(courseRepository).searchPublic(eq("교토"), argThat((Pageable p) -> p.getPageSize() == 20));
    }
}
