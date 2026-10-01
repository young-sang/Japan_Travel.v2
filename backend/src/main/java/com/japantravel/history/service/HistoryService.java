package com.japantravel.history.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.destination.entity.Destination;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.festival.entity.Festival;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.history.dto.HistoryItemResponse;
import com.japantravel.history.entity.HistoryDestination;
import com.japantravel.history.entity.HistoryFestival;
import com.japantravel.history.repository.HistoryDestinationRepository;
import com.japantravel.history.repository.HistoryFestivalRepository;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

//  기록은 upsert 다 — 이미 본 대상이면 visitedAt 만 갱신하고, 처음이면 행을 만든다 (D-052).
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryService {

    private final HistoryDestinationRepository historyDestinationRepository;
    private final HistoryFestivalRepository historyFestivalRepository;
    private final UserRepository userRepository;
//  대상 도메인은 존재 확인과 참조용으로만 읽는다
    private final DestinationRepository destinationRepository;
    private final FestivalRepository festivalRepository;

//  두 테이블을 각각 읽어 한 배열로 합친다. 화면이 "오늘 / 어제 / ..." 로 섞어서 묶기 때문이다.
    public List<HistoryItemResponse> findMine(Long userId) {
        return Stream.concat(
                        historyDestinationRepository.findByUserIdOrderByVisitedAtDesc(userId).stream()
                                .map(HistoryItemResponse::from),
                        historyFestivalRepository.findByUserIdOrderByVisitedAtDesc(userId).stream()
                                .map(HistoryItemResponse::from))
                .sorted(Comparator.comparing(HistoryItemResponse::visitedAt).reversed())
                .toList();
    }

    @Transactional
    public void touchDestination(Long userId, Long destinationId) {
        historyDestinationRepository.findByUserIdAndDestinationId(userId, destinationId)
                .ifPresentOrElse(HistoryDestination::touch, () -> {
                    Destination destination = destinationRepository.findById(destinationId)
                            .orElseThrow(() -> new ApiException(ErrorCode.DESTINATION_NOT_FOUND));
                    historyDestinationRepository.save(new HistoryDestination(findUser(userId), destination));
                });
    }

    @Transactional
    public void touchFestival(Long userId, Long festivalId) {
        historyFestivalRepository.findByUserIdAndFestivalId(userId, festivalId)
                .ifPresentOrElse(HistoryFestival::touch, () -> {
                    Festival festival = festivalRepository.findById(festivalId)
                            .orElseThrow(() -> new ApiException(ErrorCode.FESTIVAL_NOT_FOUND));
                    historyFestivalRepository.save(new HistoryFestival(findUser(userId), festival));
                });
    }

    @Transactional
    public void clearMine(Long userId) {
        historyDestinationRepository.deleteAllByUserId(userId);
        historyFestivalRepository.deleteAllByUserId(userId);
    }

//  토큰은 유효한데 사용자가 지워졌으면 401 — FavoriteService 와 같은 규칙.
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
    }
}
