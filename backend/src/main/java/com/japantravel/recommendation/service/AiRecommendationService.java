package com.japantravel.recommendation.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.prefecture.repository.PrefectureRepository;
import com.japantravel.recommendation.ai.LlmPicker;
import com.japantravel.recommendation.dto.AiRecommendationRequest;
import com.japantravel.recommendation.dto.AiRecommendationResponse;
import com.japantravel.recommendation.dto.RecommendationTargetType;
import com.japantravel.recommendation.repository.RecommendationRepository;
import com.japantravel.recommendation.repository.RecommendationRepository.CardInfo;
import com.japantravel.recommendation.repository.RecommendationRepository.DestinationCandidate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//  AI 추천 페이지 (M4-1): 조건(도도부현)으로 여행지 후보를 추림 → LLM 이 2~3곳 + 이유 → 후보에 없는 id 를 버림
//  (결정: recommender/2026-11-15-ai-page-filter-then-llm)
@Service
public class AiRecommendationService {

    static final int MAX_PREFERENCE_LENGTH = 200;
    static final int MAX_PICKS = 3;

    private final RecommendationRepository repository;
    private final PrefectureRepository prefectureRepository;
    private final LlmPicker llmPicker;
    private final int maxCandidates;

    public AiRecommendationService(RecommendationRepository repository, PrefectureRepository prefectureRepository,
                                   LlmPicker llmPicker, @Value("${llm.max-candidates}") int maxCandidates) {
        this.repository = repository;
        this.prefectureRepository = prefectureRepository;
        this.llmPicker = llmPicker;
        this.maxCandidates = maxCandidates;
    }

    public List<AiRecommendationResponse> recommend(AiRecommendationRequest request) {
        String preference = request.preference() == null ? "" : request.preference().trim();
        if (preference.isEmpty() || preference.length() > MAX_PREFERENCE_LENGTH || request.prefecture() == null) {
            throw new ApiException(ErrorCode.INVALID_AI_REQUEST);
        }
        if (!prefectureRepository.existsByName(request.prefecture())) {
            throw new ApiException(ErrorCode.PREFECTURE_NOT_FOUND);
        }

        List<DestinationCandidate> candidates =
                repository.findDestinationsByPrefecture(request.prefecture(), maxCandidates);
        if (candidates.isEmpty()) {
            throw new ApiException(ErrorCode.AI_RECOMMEND_FAILED);   // 상황별 문구는 M4-3
        }
        Map<Long, DestinationCandidate> byId = candidates.stream()
                .collect(Collectors.toMap(DestinationCandidate::id, Function.identity()));

        List<LlmPicker.Pick> picks = llmPicker.pick(preference, candidates.stream()
                .map(c -> new LlmPicker.Candidate(c.id(), c.name(), c.description()))
                .toList());

//      검증: 후보 안의 id 만, 같은 장소는 처음 것만, 최대 3곳
        Map<Long, String> valid = new LinkedHashMap<>();
        for (LlmPicker.Pick p : picks) {
            if (byId.containsKey(p.id()) && !valid.containsKey(p.id()) && valid.size() < MAX_PICKS) {
                valid.put(p.id(), p.reason());
            }
        }
        if (valid.isEmpty()) {
            throw new ApiException(ErrorCode.AI_RECOMMEND_FAILED);
        }

        Map<Long, CardInfo> cards = repository.findCardInfo(new ArrayList<>(valid.keySet()), List.of()).stream()
                .collect(Collectors.toMap(CardInfo::placeId, Function.identity()));
        return valid.entrySet().stream()
                .map(e -> {
                    DestinationCandidate c = byId.get(e.getKey());
                    CardInfo card = cards.get(e.getKey());
                    return new AiRecommendationResponse(RecommendationTargetType.DESTINATION, c.id(), c.name(),
                            c.prefecture(), c.imagePath(),
                            card == null ? null : card.avgRating(), card == null ? 0 : card.reviewCount(),
                            e.getValue());
                })
                .toList();
    }
}
