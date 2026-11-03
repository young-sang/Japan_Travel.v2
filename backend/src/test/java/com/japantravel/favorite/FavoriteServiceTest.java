package com.japantravel.favorite;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.destination.entity.Destination;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.favorite.entity.FavoriteDestination;
import com.japantravel.favorite.repository.FavoriteDestinationRepository;
import com.japantravel.favorite.repository.FavoriteFestivalRepository;
import com.japantravel.favorite.service.FavoriteService;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.japantravel.support.Fixtures.destination;
import static com.japantravel.support.Fixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

//  추가 · 삭제는 멱등이다
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock FavoriteDestinationRepository favoriteDestinationRepository;
    @Mock FavoriteFestivalRepository favoriteFestivalRepository;
    @Mock UserRepository userRepository;
    @Mock DestinationRepository destinationRepository;
    @Mock FestivalRepository festivalRepository;

    @InjectMocks FavoriteService favoriteService;

    @Test
    void 이미_찜했으면_대상을_읽지도_저장하지도_않는다() {
        when(favoriteDestinationRepository.existsByUserIdAndDestinationId(1L, 10L)).thenReturn(true);

        favoriteService.addDestination(1L, 10L);

        verify(favoriteDestinationRepository, never()).save(any());
        verifyNoInteractions(destinationRepository, userRepository);
    }

    @Test
    void 처음이면_사용자와_대상을_묶어_저장한다() {
        when(favoriteDestinationRepository.existsByUserIdAndDestinationId(1L, 10L)).thenReturn(false);
        Destination d10 = destination(10L);
        when(destinationRepository.findById(10L)).thenReturn(Optional.of(d10));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));

        favoriteService.addDestination(1L, 10L);

        ArgumentCaptor<FavoriteDestination> saved = ArgumentCaptor.forClass(FavoriteDestination.class);
        verify(favoriteDestinationRepository).save(saved.capture());
        assertThat(saved.getValue().getUser().getId()).isEqualTo(1L);
        assertThat(saved.getValue().getDestination().getId()).isEqualTo(10L);
    }

    @Test
    void 없는_축제를_찜하면_FESTIVAL_NOT_FOUND() {
        when(favoriteFestivalRepository.existsByUserIdAndFestivalId(1L, 20L)).thenReturn(false);
        when(festivalRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.addFestival(1L, 20L)).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(ErrorCode.FESTIVAL_NOT_FOUND);
    }

    @Test
    void 삭제는_있는지_보지_않고_지운다() {
        favoriteService.removeDestination(1L, 10L);

        verify(favoriteDestinationRepository).deleteByUserIdAndDestinationId(1L, 10L);
        verify(favoriteDestinationRepository, never()).existsByUserIdAndDestinationId(any(), any());
    }
}
