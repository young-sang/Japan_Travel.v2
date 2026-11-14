package com.japantravel.recommendation.service;

import com.japantravel.recommendation.dto.RecommendationResponse;
import com.japantravel.recommendation.dto.RecommendationTargetType;
import com.japantravel.recommendation.repository.RecommendationRepository;
import com.japantravel.recommendation.repository.RecommendationRepository.Activity;
import com.japantravel.recommendation.repository.RecommendationRepository.ActivityKind;
import com.japantravel.recommendation.repository.RecommendationRepository.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//  활동 기록 → 사용자 벡터 → 비슷한 장소 5곳 (D-098 · D-099)
//  사용자 벡터 = 활동한 장소들의 임베딩을 활동 가중치로 평균한 것. 그 벡터와 코사인 유사도가 높은 순으로 고른다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

//  즐겨찾기 > 리뷰 > 최근 본 장소. 한 장소에 여러 활동이 있으면 더한다 (예: 즐겨찾기 + 리뷰 = 5)
    static final Map<ActivityKind, Double> WEIGHT = Map.of(
            ActivityKind.FAVORITE, 3.0,
            ActivityKind.REVIEW, 2.0,
            ActivityKind.HISTORY, 1.0);
//  5곳 = 여행지 4 + 축제 1, 같은 현은 최대 2곳 (D-099)
    static final Map<RecommendationTargetType, Integer> PER_TYPE = Map.of(
            RecommendationTargetType.DESTINATION, 4,
            RecommendationTargetType.FESTIVAL, 1);
    static final int PER_PREFECTURE = 2;

    private record Key(RecommendationTargetType type, long id) {
    }

    private record Scored(Place place, double similarity) {
    }

    private final RecommendationRepository repository;

    public List<RecommendationResponse> recommend(long userId) {
//      1. 활동 → 장소별 가중치 합
        Map<Key, Double> weights = new HashMap<>();
        for (Activity a : repository.findActivities(userId)) {
            weights.merge(new Key(a.type(), a.placeId()), WEIGHT.get(a.kind()), Double::sum);
        }
        if (weights.isEmpty()) {
            return List.of();
        }

//      2. 사용자 벡터 = 활동 장소 벡터의 가중 평균 (임베딩이 없는 활동 장소는 빠진다)
        List<Place> places = repository.findAllPlaces();
        float[] user = null;
        double total = 0;
        for (Place p : places) {
            Double w = weights.get(new Key(p.type(), p.id()));
            if (w == null) {
                continue;
            }
            if (user == null) {
                user = new float[p.vector().length];
            }
            for (int i = 0; i < user.length; i++) {
                user[i] += (float) (w * p.vector()[i]);
            }
            total += w;
        }
        if (user == null) {
            return List.of();
        }
        for (int i = 0; i < user.length; i++) {
            user[i] /= (float) total;
        }
        float[] userVector = user;
        double userNorm = Math.sqrt(dot(userVector, userVector));

//      3. 활동하지 않은 장소와 코사인 유사도. 장소 벡터는 이미 길이 1 이라 |u| 로만 나눈다
        List<Scored> ranked = places.stream()
                .filter(p -> !weights.containsKey(new Key(p.type(), p.id())))
                .map(p -> new Scored(p, dot(userVector, p.vector()) / userNorm))
                .sorted(Comparator.comparingDouble(Scored::similarity).reversed())
                .toList();

//      4. 순위 규칙으로 5곳
        return pick(ranked).stream()
                .map(s -> new RecommendationResponse(s.place().type(), s.place().id(), s.place().name(),
                        s.place().prefecture(), s.place().imagePath(), s.similarity()))
                .toList();
    }

//  유사도 높은 순으로 훑으면서, 그 종류의 칸이 남았고 그 현이 아직 PER_PREFECTURE 곳 미만이면 고른다.
//  훑는 순서 그대로 담으므로 결과도 유사도 순이고 여행지 · 축제가 섞여 있다. 후보가 모자라면 고른 만큼만 준다
    private static List<Scored> pick(List<Scored> ranked) {
        Map<RecommendationTargetType, Integer> left = new HashMap<>(PER_TYPE);
        Map<String, Integer> perPrefecture = new HashMap<>();
        int remaining = PER_TYPE.values().stream().mapToInt(Integer::intValue).sum();
        List<Scored> picked = new ArrayList<>();
        for (Scored s : ranked) {
            if (remaining == 0) {
                break;
            }
            Place p = s.place();
            if (left.get(p.type()) == 0 || perPrefecture.getOrDefault(p.prefecture(), 0) >= PER_PREFECTURE) {
                continue;
            }
            picked.add(s);
            left.merge(p.type(), -1, Integer::sum);
            perPrefecture.merge(p.prefecture(), 1, Integer::sum);
            remaining--;
        }
        return picked;
    }

    private static double dot(float[] a, float[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }
}
