package com.japantravel.festival;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.festival.service.FestivalService;
import com.japantravel.prefecture.repository.PrefectureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FestivalServiceTest {

    @Mock FestivalRepository festivalRepository;
    @Mock PrefectureRepository prefectureRepository;

    @InjectMocks FestivalService festivalService;

    @ParameterizedTest
    @ValueSource(ints = {0, 13, -1})
    void 월이_1에서_12_밖이면_현을_보기_전에_INVALID_MONTH(int month) {
        assertThatThrownBy(() -> festivalService.findAll("없는현", month)).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_MONTH);
        verifyNoInteractions(prefectureRepository, festivalRepository);
    }

    @Test
    void 없는_현이면_PREFECTURE_NOT_FOUND() {
        when(prefectureRepository.existsByName("없는현")).thenReturn(false);

        assertThatThrownBy(() -> festivalService.findAll("없는현", null)).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(ErrorCode.PREFECTURE_NOT_FOUND);
    }

//  null 조건을 쿼리에 넘기면 WHERE month = NULL 로 0건이 되므로 조합마다 다른 메서드를 쓴다
    @Test
    void 필터_조합마다_맞는_쿼리를_부른다() {
        when(prefectureRepository.existsByName("교토부")).thenReturn(true);

        festivalService.findAll(null, null);
        festivalService.findAll(" ", 7);
        festivalService.findAll("교토부", null);
        festivalService.findAll("교토부", 7);

        verify(festivalRepository).findAllByOrderByMonthAscIdDesc();
        verify(festivalRepository).findByMonthOrderByMonthAscIdDesc(7);
        verify(festivalRepository).findByPrefectureNameOrderByMonthAscIdDesc("교토부");
        verify(festivalRepository).findByPrefectureNameAndMonthOrderByMonthAscIdDesc("교토부", 7);
    }
}
