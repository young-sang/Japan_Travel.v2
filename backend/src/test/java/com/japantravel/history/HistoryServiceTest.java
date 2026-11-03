package com.japantravel.history;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.festival.entity.Festival;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.history.dto.HistoryItemResponse;
import com.japantravel.history.dto.HistoryTargetType;
import com.japantravel.history.entity.HistoryDestination;
import com.japantravel.history.entity.HistoryFestival;
import com.japantravel.history.repository.HistoryDestinationRepository;
import com.japantravel.history.repository.HistoryFestivalRepository;
import com.japantravel.history.service.HistoryService;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.japantravel.support.Fixtures.destination;
import static com.japantravel.support.Fixtures.festival;
import static com.japantravel.support.Fixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

//  기록은 upsert 다 (D-052)
@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock HistoryDestinationRepository historyDestinationRepository;
    @Mock HistoryFestivalRepository historyFestivalRepository;
    @Mock UserRepository userRepository;
    @Mock DestinationRepository destinationRepository;
    @Mock FestivalRepository festivalRepository;

    @InjectMocks HistoryService historyService;

    @Test
    void 이미_본_대상이면_시각만_갱신하고_새로_만들지_않는다() {
        HistoryDestination seen = new HistoryDestination(user(1L), destination(10L));
        LocalDateTime before = LocalDateTime.now().minusDays(1);
        ReflectionTestUtils.setField(seen, "visitedAt", before);
        when(historyDestinationRepository.findByUserIdAndDestinationId(1L, 10L)).thenReturn(Optional.of(seen));

        historyService.touchDestination(1L, 10L);

        assertThat(seen.getVisitedAt()).isAfter(before);
        verify(historyDestinationRepository, never()).save(any());
        verifyNoInteractions(destinationRepository, userRepository);
    }

    @Test
    void 처음_본_대상이면_새로_만든다() {
        when(historyFestivalRepository.findByUserIdAndFestivalId(1L, 20L)).thenReturn(Optional.empty());
        Festival f20 = festival(20L);
        when(festivalRepository.findById(20L)).thenReturn(Optional.of(f20));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));

        historyService.touchFestival(1L, 20L);

        verify(historyFestivalRepository).save(any(HistoryFestival.class));
    }

    @Test
    void 처음_본_대상이_없으면_404() {
        when(historyDestinationRepository.findByUserIdAndDestinationId(1L, 10L)).thenReturn(Optional.empty());
        when(destinationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.touchDestination(1L, 10L)).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(ErrorCode.DESTINATION_NOT_FOUND);
    }

    @Test
    void 두_테이블을_섞어_최근에_본_것부터() {
        User me = user(1L);
        HistoryDestination d = new HistoryDestination(me, destination(10L));
        HistoryFestival f = new HistoryFestival(me, festival(20L));
        ReflectionTestUtils.setField(d, "visitedAt", LocalDateTime.now().minusHours(2));
        ReflectionTestUtils.setField(f, "visitedAt", LocalDateTime.now().minusHours(1));
        when(historyDestinationRepository.findByUserIdOrderByVisitedAtDesc(1L)).thenReturn(List.of(d));
        when(historyFestivalRepository.findByUserIdOrderByVisitedAtDesc(1L)).thenReturn(List.of(f));

        List<HistoryItemResponse> mine = historyService.findMine(1L);

        assertThat(mine).extracting(HistoryItemResponse::type)
                .containsExactly(HistoryTargetType.FESTIVAL, HistoryTargetType.DESTINATION);
        assertThat(mine.get(0).prefecture()).isEqualTo("교토부");
    }

    @Test
    void 전체_삭제는_두_테이블을_모두_지운다() {
        historyService.clearMine(1L);

        verify(historyDestinationRepository).deleteAllByUserId(1L);
        verify(historyFestivalRepository).deleteAllByUserId(1L);
    }
}
