package com.japantravel.destination.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.destination.dto.DestinationRankingResponse;
import com.japantravel.destination.dto.DestinationResponse;
import com.japantravel.destination.entity.Destination;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.prefecture.repository.PrefectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final PrefectureRepository prefectureRepository;

    private static final int RANKING_SIZE = 25;

//  DTO 로 바꿔서 내보낸다. open-in-view 가 꺼져 있어 컨트롤러에서 LAZY 인
//  prefecture 를 건드리면 LazyInitializationException 이 난다.
    public List<DestinationResponse> findAll(String prefecture) {
        if (prefecture == null || prefecture.isBlank()) {
            return toResponses(destinationRepository.findAllByOrderByIdDesc());
        }
//      없는 현 이름은 404. 실재하는 현인데 0건이면 빈 목록으로 내려간다.
        if (!prefectureRepository.existsByName(prefecture)) {
            throw new ApiException(ErrorCode.PREFECTURE_NOT_FOUND);
        }
        return toResponses(destinationRepository.findByPrefectureNameOrderByIdDesc(prefecture));
    }

    public DestinationResponse findById(Long id) {
        return destinationRepository.findById(id)
                .map(DestinationResponse::from)
                .orElseThrow(() -> new ApiException(ErrorCode.DESTINATION_NOT_FOUND));
    }

//  순위는 쿼리가 정하고, 여행지 본문은 id 로 한 번에 가져와 순위 순서대로 붙인다
    public List<DestinationRankingResponse> ranking() {
        List<DestinationRepository.RankingRow> rows = destinationRepository.ranking(RANKING_SIZE);
        Map<Long, Destination> byId = destinationRepository.findByIdIn(
                        rows.stream().map(DestinationRepository.RankingRow::getId).toList()).stream()
                .collect(Collectors.toMap(Destination::getId, Function.identity()));
        return IntStream.range(0, rows.size())
                .mapToObj(i -> {
                    DestinationRepository.RankingRow r = rows.get(i);
                    return new DestinationRankingResponse(i + 1, r.getScore(), r.getViewerCount(),
                            r.getFavoriteCount(), DestinationResponse.from(byId.get(r.getId())));
                })
                .toList();
    }

    private List<DestinationResponse> toResponses(List<Destination> found) {
        return found.stream().map(DestinationResponse::from).toList();
    }
}
