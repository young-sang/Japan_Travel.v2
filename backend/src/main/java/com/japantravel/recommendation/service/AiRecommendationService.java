package com.japantravel.recommendation.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.prefecture.repository.PrefectureRepository;
import com.japantravel.recommendation.ai.AiDailyLimiter;
import com.japantravel.recommendation.ai.LlmPicker;
import com.japantravel.recommendation.dto.AiPlaceKind;
import com.japantravel.recommendation.dto.AiRecommendationRequest;
import com.japantravel.recommendation.dto.AiRecommendationResponse;
import com.japantravel.recommendation.dto.RecommendationTargetType;
import com.japantravel.recommendation.dto.Season;
import com.japantravel.recommendation.repository.RecommendationRepository;
import com.japantravel.recommendation.repository.RecommendationRepository.CardInfo;
import com.japantravel.recommendation.repository.RecommendationRepository.PlaceCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//  AI 추천 페이지: 조건(도도부현 · 종류 · 계절)으로 후보를 추림 → LLM 이 2~3곳 + 이유 → 후보에 없는 키를 버림
//  (결정: recommender/2026-11-15-ai-page-filter-then-llm). M4-1 여행지, M4-2 축제 · 계절
@Slf4j
@Service
public class AiRecommendationService {

    static final int MAX_PREFERENCE_LENGTH = 200;
    static final int MAX_PICKS = 3;

    private final RecommendationRepository repository;
    private final PrefectureRepository prefectureRepository;
    private final LlmPicker llmPicker;
    private final AiDailyLimiter limiter;
    private final int maxCandidates;

    public AiRecommendationService(RecommendationRepository repository, PrefectureRepository prefectureRepository,
                                   LlmPicker llmPicker, AiDailyLimiter limiter,
                                   @Value("${llm.max-candidates}") int maxCandidates) {
        this.repository = repository;
        this.prefectureRepository = prefectureRepository;
        this.llmPicker = llmPicker;
        this.limiter = limiter;
        this.maxCandidates = maxCandidates;
    }

//  LLM 에 넘기는 키. 여행지와 축제는 id 가 겹치므로 종류 글자를 붙인다 (M4-2)
    static String key(RecommendationTargetType type, long id) {
        return (type == RecommendationTargetType.DESTINATION ? "D" : "F") + id;
    }

    public List<AiRecommendationResponse> recommend(AiRecommendationRequest request) {
        String preference = request.preference() == null ? "" : request.preference().trim();
        if (preference.isEmpty() || preference.length() > MAX_PREFERENCE_LENGTH || request.prefecture() == null) {
            throw new ApiException(ErrorCode.INVALID_AI_REQUEST);
        }
        if (!prefectureRepository.existsByName(request.prefecture())) {
            throw new ApiException(ErrorCode.PREFECTURE_NOT_FOUND);
        }
        AiPlaceKind kind = request.kind() == null ? AiPlaceKind.ALL : request.kind();
        Season season = request.season();

//      여행지는 계절로 거르지 않는다 (시기 데이터 없음, LLM 이 판단). 축제는 개최 월로 거른다
        List<PlaceCandidate> candidates = new ArrayList<>();
        if (kind.includesDestinations()) {
            candidates.addAll(repository.findDestinationsByPrefecture(request.prefecture(), maxCandidates));
        }
        if (kind.includesFestivals()) {
            candidates.addAll(repository.findFestivalsByPrefecture(request.prefecture(),
                    season == null ? List.of() : season.months(), maxCandidates));
        }
        if (candidates.isEmpty()) {
            throw new ApiException(ErrorCode.NO_AI_CANDIDATES);   // LLM 을 부르지 않는다 (M4-3)
        }
        Map<String, PlaceCandidate> byKey = candidates.stream()
                .collect(Collectors.toMap(c -> key(c.type(), c.id()), Function.identity()));

//      하루 상한은 LLM 을 부르기 바로 앞에서 센다 — 입력 오류 · 후보 0곳은 세지 않는다 (M4-4)
        if (!limiter.tryAcquire()) {
            throw new ApiException(ErrorCode.AI_DAILY_LIMIT_EXCEEDED);
        }
        List<LlmPicker.Pick> picks = llmPicker.pick(preference, season == null ? null : season.label(),
                candidates.stream()
                        .map(c -> new LlmPicker.Candidate(key(c.type(), c.id()),
                                c.type() == RecommendationTargetType.DESTINATION ? "여행지" : "축제",
                                c.name(), c.description()))
                        .toList());

//      검증: 후보 안의 키만, 같은 장소는 처음 것만, 최대 3곳
        Map<String, String> valid = new LinkedHashMap<>();
        for (LlmPicker.Pick p : picks) {
            if (byKey.containsKey(p.key()) && !valid.containsKey(p.key()) && valid.size() < MAX_PICKS) {
                valid.put(p.key(), p.reason());
            }
        }
        if (valid.isEmpty()) {
            log.warn("LLM 이 후보 밖만 골랐다: {}", picks.stream().map(LlmPicker.Pick::key).toList());
            throw new ApiException(ErrorCode.AI_RECOMMEND_FAILED);
        }

        List<PlaceCandidate> chosen = valid.keySet().stream().map(byKey::get).toList();
        Map<String, CardInfo> cards = repository.findCardInfo(
                        idsOf(chosen, RecommendationTargetType.DESTINATION),
                        idsOf(chosen, RecommendationTargetType.FESTIVAL)).stream()
                .collect(Collectors.toMap(c -> key(c.type(), c.placeId()), Function.identity()));
        return chosen.stream()
                .map(c -> {
                    String k = key(c.type(), c.id());
                    CardInfo card = cards.get(k);
                    return new AiRecommendationResponse(c.type(), c.id(), c.name(), c.prefecture(), c.imagePath(),
                            card == null ? null : card.avgRating(), card == null ? 0 : card.reviewCount(),
                            valid.get(k));
                })
                .toList();
    }

    private static List<Long> idsOf(List<PlaceCandidate> places, RecommendationTargetType type) {
        return places.stream().filter(c -> c.type() == type).map(PlaceCandidate::id).toList();
    }
}
