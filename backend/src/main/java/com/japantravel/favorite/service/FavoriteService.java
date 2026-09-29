package com.japantravel.favorite.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.destination.dto.DestinationResponse;
import com.japantravel.destination.entity.Destination;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.favorite.dto.FavoriteListResponse;
import com.japantravel.favorite.entity.FavoriteDestination;
import com.japantravel.favorite.entity.FavoriteFestival;
import com.japantravel.favorite.repository.FavoriteDestinationRepository;
import com.japantravel.favorite.repository.FavoriteFestivalRepository;
import com.japantravel.festival.dto.FestivalResponse;
import com.japantravel.festival.entity.Festival;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//  추가·삭제는 멱등이다 — 이미 있으면 추가하지 않고, 없으면 지우지 않고, 둘 다 성공으로 끝난다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteDestinationRepository favoriteDestinationRepository;
    private final FavoriteFestivalRepository favoriteFestivalRepository;
    private final UserRepository userRepository;
//  대상 도메인은 존재 확인과 참조용으로만 읽는다
    private final DestinationRepository destinationRepository;
    private final FestivalRepository festivalRepository;

    public FavoriteListResponse findMine(Long userId) {
        return new FavoriteListResponse(
                favoriteDestinationRepository.findByUserIdOrderByIdDesc(userId).stream()
                        .map(f -> DestinationResponse.from(f.getDestination()))
                        .toList(),
                favoriteFestivalRepository.findByUserIdOrderByIdDesc(userId).stream()
                        .map(f -> FestivalResponse.from(f.getFestival()))
                        .toList()
        );
    }

    @Transactional
    public void addDestination(Long userId, Long destinationId) {
        if (favoriteDestinationRepository.existsByUserIdAndDestinationId(userId, destinationId)) {
            return;
        }
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ApiException(ErrorCode.DESTINATION_NOT_FOUND));
        favoriteDestinationRepository.save(new FavoriteDestination(findUser(userId), destination));
    }

    @Transactional
    public void removeDestination(Long userId, Long destinationId) {
        favoriteDestinationRepository.deleteByUserIdAndDestinationId(userId, destinationId);
    }

    @Transactional
    public void addFestival(Long userId, Long festivalId) {
        if (favoriteFestivalRepository.existsByUserIdAndFestivalId(userId, festivalId)) {
            return;
        }
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new ApiException(ErrorCode.FESTIVAL_NOT_FOUND));
        favoriteFestivalRepository.save(new FavoriteFestival(findUser(userId), festival));
    }

    @Transactional
    public void removeFestival(Long userId, Long festivalId) {
        favoriteFestivalRepository.deleteByUserIdAndFestivalId(userId, festivalId);
    }

//  토큰은 유효한데 사용자가 지워졌으면 401 — AuthService.me 와 같은 규칙.
//  확인하지 않으면 FK 위반으로 500 이 된다.
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
    }
}
