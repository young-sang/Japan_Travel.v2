package com.japantravel.destination;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.destination.dto.DestinationResponse;
import com.japantravel.destination.entity.Destination;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.destination.service.DestinationService;
import com.japantravel.prefecture.repository.PrefectureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.japantravel.support.Fixtures.destination;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DestinationServiceTest {

    @Mock DestinationRepository destinationRepository;
    @Mock PrefectureRepository prefectureRepository;

    @InjectMocks DestinationService destinationService;

    @Test
    void 현이_비어_있으면_전체_목록() {
        destinationService.findAll(" ");

        verify(destinationRepository).findAllByOrderByIdDesc();
        verifyNoInteractions(prefectureRepository);
    }

    @Test
    void 없는_현이면_PREFECTURE_NOT_FOUND() {
        when(prefectureRepository.existsByName("없는현")).thenReturn(false);

        assertThatThrownBy(() -> destinationService.findAll("없는현")).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(ErrorCode.PREFECTURE_NOT_FOUND);
    }

//  평균 별점은 소수 첫째 자리로 반올림, 리뷰가 없으면 null (D-057)
    @Test
    void 평균_별점은_소수_첫째_자리로_반올림한다() {
        Destination rated = destination(1L);
        when(rated.getAvgRating()).thenReturn(4.666);
        when(rated.getReviewCount()).thenReturn(3L);
        Destination unrated = destination(2L);

        assertThat(DestinationResponse.from(rated).avgRating()).isEqualTo(4.7);
        assertThat(DestinationResponse.from(rated).reviewCount()).isEqualTo(3L);
        assertThat(DestinationResponse.from(unrated).avgRating()).isNull();
    }
}
