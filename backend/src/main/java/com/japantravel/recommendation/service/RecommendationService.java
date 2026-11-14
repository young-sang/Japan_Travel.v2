package com.japantravel.recommendation.service;

import com.japantravel.recommendation.dto.ActivityKind;
import com.japantravel.recommendation.dto.RecommendationResponse;
import com.japantravel.recommendation.dto.RecommendationTargetType;
import com.japantravel.recommendation.repository.RecommendationRepository;
import com.japantravel.recommendation.repository.RecommendationRepository.Activity;
import com.japantravel.recommendation.repository.RecommendationRepository.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//  활동 기록 → 사용자 벡터 → 비슷한 장소 5곳 + 선정 이유 (D-098 · D-099 · D-100)
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

//  내가 활동한 장소 — 가중치 합과 가장 강한 활동. 사용자 벡터와 선정 이유의 재료
    private record Liked(Place place, double weight, ActivityKind kind) {
    }

    private final RecommendationRepository repository;

    public List<RecommendationResponse> recommend(long userId) {
//      1. 활동 → 장소별 가중치 합 · 가장 강한 활동 (enum 앞쪽이 강하다)
        Map<Key, Double> weights = new HashMap<>();
        Map<Key, ActivityKind> strongest = new HashMap<>();
        for (Activity a : repository.findActivities(userId)) {
            Key key = new Key(a.type(), a.placeId());
            weights.merge(key, WEIGHT.get(a.kind()), Double::sum);
            strongest.merge(key, a.kind(), (x, y) -> x.ordinal() <= y.ordinal() ? x : y);
        }
        if (weights.isEmpty()) {
            return List.of();
        }

//      2. 사용자 벡터 = 활동 장소 벡터의 가중 평균 (임베딩이 없는 활동 장소는 빠진다)
        List<Place> places = repository.findAllPlaces();
        List<Liked> liked = new ArrayList<>();
        float[] user = null;
        double total = 0;
        for (Place p : places) {
            Key key = new Key(p.type(), p.id());
            Double w = weights.get(key);
            if (w == null) {
                continue;
            }
            liked.add(new Liked(p, w, strongest.get(key)));
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
//      5. 곳마다 선정 이유
        return pick(ranked).stream()
                .map(s -> toResponse(s, basisOf(s.place(), liked)))
                .toList();
    }

//  선정 이유의 근거 = 내 활동 장소 중 (이 장소와의 유사도 × 활동 가중치) 가 가장 큰 곳.
//  가중치를 곱해서, 조금 덜 비슷해도 즐겨찾기한 곳이 근거로 나오기 쉽다 (D-100)
    private static Liked basisOf(Place place, List<Liked> liked) {
        Liked best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Liked l : liked) {
            double score = dot(place.vector(), l.place().vector()) * l.weight();
            if (score > bestScore) {
                best = l;
                bestScore = score;
            }
        }
        return best;
    }

    private static RecommendationResponse toResponse(Scored s, Liked basis) {
        Place p = s.place();
        Place b = basis.place();
        return new RecommendationResponse(p.type(), p.id(), p.name(), p.prefecture(), p.imagePath(), s.similarity(),
                RecommendationReason.write(basis.kind(), b.name()),
                new RecommendationResponse.Basis(b.type(), b.id(), b.name(), basis.kind()));
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
