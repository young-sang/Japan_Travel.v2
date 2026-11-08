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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    void 대상별로_20건으로_자른다() {
        when(destinationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        when(festivalRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        when(courseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        searchService.search("  교토 단풍 ");

        verify(destinationRepository).findAll(any(Specification.class), argThat((Pageable p) -> p.getPageSize() == 20));
        verify(festivalRepository).findAll(any(Specification.class), argThat((Pageable p) -> p.getPageSize() == 20));
        verify(courseRepository).findAll(any(Specification.class), argThat((Pageable p) -> p.getPageSize() == 20));
    }
}
