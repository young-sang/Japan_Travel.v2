package com.japantravel.recommendation.repository;

import com.japantravel.recommendation.dto.ActivityKind;
import com.japantravel.recommendation.dto.RecommendationTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//  임베딩 테이블에는 엔티티가 없다 (읽기 전용 BLOB, D-096). SQL 두 개라 JdbcTemplate 으로 읽는다 (D-098)
@Repository
@RequiredArgsConstructor
public class RecommendationRepository {

//  disliked = 이 장소에 1~2점 리뷰를 남겼다 (D-103)
    public record Activity(RecommendationTargetType type, long placeId, ActivityKind kind, boolean disliked) {
    }

//  다른 사용자들의 평균 별점 — 리뷰 2건 이상인 장소만 (D-103)
    public record Rating(RecommendationTargetType type, long placeId, double average) {
    }

    public record Place(RecommendationTargetType type, long id, String name, String prefecture,
                        String imagePath, float[] vector) {
    }

//  카드에 보여 줄 정보 — 고른 몇 곳만 읽는다. 별점은 엔티티 @Formula 와 같은 기준 (D-057, 리뷰 1건 이상이면 평균)
    public record CardInfo(RecommendationTargetType type, long placeId, String description,
                           Double avgRating, long reviewCount) {
    }

//  AI 추천 페이지 후보 (M4-1). 설명은 프롬프트용 앞 200자
    public record DestinationCandidate(long id, String name, String prefecture, String imagePath,
                                       String description) {
    }

    private final JdbcTemplate jdbc;

//  사용자가 활동한 장소. 리뷰는 한 장소에 여러 개여도 장소당 한 번, 평균 2점 이하면 disliked
    public List<Activity> findActivities(long userId) {
        return jdbc.query("""
                SELECT 'DESTINATION' AS type, destination_id AS place_id, 'FAVORITE' AS kind, 0 AS disliked FROM favorite_destinations WHERE user_id = ?
                UNION ALL SELECT 'FESTIVAL', festival_id, 'FAVORITE', 0 FROM favorite_festivals WHERE user_id = ?
                UNION ALL SELECT 'DESTINATION', destination_id, 'REVIEW', AVG(rating) <= 2 FROM review_destinations WHERE user_id = ? GROUP BY destination_id
                UNION ALL SELECT 'FESTIVAL', festival_id, 'REVIEW', AVG(rating) <= 2 FROM review_festivals WHERE user_id = ? GROUP BY festival_id
                UNION ALL SELECT 'DESTINATION', destination_id, 'HISTORY', 0 FROM history_destinations WHERE user_id = ?
                UNION ALL SELECT 'FESTIVAL', festival_id, 'HISTORY', 0 FROM history_festivals WHERE user_id = ?
                """,
                (rs, i) -> new Activity(RecommendationTargetType.valueOf(rs.getString("type")),
                        rs.getLong("place_id"), ActivityKind.valueOf(rs.getString("kind")), rs.getBoolean("disliked")),
                userId, userId, userId, userId, userId, userId);
    }

    public List<Rating> findRatings() {
        return jdbc.query("""
                SELECT 'DESTINATION' AS type, destination_id AS place_id, AVG(rating) AS average
                FROM review_destinations GROUP BY destination_id HAVING COUNT(*) >= 2
                UNION ALL
                SELECT 'FESTIVAL', festival_id, AVG(rating) FROM review_festivals GROUP BY festival_id HAVING COUNT(*) >= 2
                """,
                (rs, i) -> new Rating(RecommendationTargetType.valueOf(rs.getString("type")),
                        rs.getLong("place_id"), rs.getDouble("average")));
    }

//  임베딩이 있는 모든 장소. 임베딩이 없는 장소는 JOIN 에서 빠져 추천 대상이 되지 않는다
    public List<Place> findAllPlaces() {
        return jdbc.query("""
                SELECT 'DESTINATION' AS type, d.id, d.name, p.name AS prefecture, d.image_path, e.vector
                FROM destination_embeddings e JOIN destinations d ON d.id = e.destination_id
                JOIN prefectures p ON p.id = d.prefecture_id
                UNION ALL
                SELECT 'FESTIVAL', f.id, f.name, p.name, f.image_path, e.vector
                FROM festival_embeddings e JOIN festivals f ON f.id = e.festival_id
                JOIN prefectures p ON p.id = f.prefecture_id
                """,
                (rs, i) -> new Place(RecommendationTargetType.valueOf(rs.getString("type")), rs.getLong("id"),
                        rs.getString("name"), rs.getString("prefecture"), rs.getString("image_path"),
                        toVector(rs.getBytes("vector"))));
    }

//  설명 앞 200자 + 리뷰 평균 · 수. 1,350곳 전체를 읽는 findAllPlaces 에는 설명을 넣지 않는다 (M3-1)
    public List<CardInfo> findCardInfo(List<Long> destinationIds, List<Long> festivalIds) {
        List<CardInfo> result = new ArrayList<>();
        result.addAll(cardInfo(RecommendationTargetType.DESTINATION, "destinations", "review_destinations",
                "destination_id", destinationIds));
        result.addAll(cardInfo(RecommendationTargetType.FESTIVAL, "festivals", "review_festivals",
                "festival_id", festivalIds));
        return result;
    }

//  테이블 · 컬럼 이름은 위 상수만 들어온다 (사용자 입력 아님). id 는 ? 로 넘긴다
    private List<CardInfo> cardInfo(RecommendationTargetType type, String table, String reviewTable,
                                    String fk, List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String in = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = """
                SELECT t.id, LEFT(t.description, 200) AS description,
                       (SELECT AVG(r.rating) FROM %2$s r WHERE r.%3$s = t.id) AS avg_rating,
                       (SELECT COUNT(*) FROM %2$s r WHERE r.%3$s = t.id) AS review_count
                FROM %1$s t WHERE t.id IN (%4$s)
                """.formatted(table, reviewTable, fk, in);
        return jdbc.query(sql,
                (rs, i) -> {
                    Object avg = rs.getObject("avg_rating");
                    return new CardInfo(type, rs.getLong("id"), rs.getString("description"),
                            avg == null ? null : ((Number) avg).doubleValue(), rs.getLong("review_count"));
                },
                ids.toArray());
    }

//  한 도도부현의 여행지 — AI 추천 페이지가 LLM 에 넘길 후보 (M4-1). limit 을 넘으면 id 순으로 자른다
    public List<DestinationCandidate> findDestinationsByPrefecture(String prefecture, int limit) {
        return jdbc.query("""
                SELECT d.id, d.name, p.name AS prefecture, d.image_path, LEFT(d.description, 200) AS description
                FROM destinations d JOIN prefectures p ON p.id = d.prefecture_id
                WHERE p.name = ?
                ORDER BY d.id
                LIMIT ?
                """,
                (rs, i) -> new DestinationCandidate(rs.getLong("id"), rs.getString("name"),
                        rs.getString("prefecture"), rs.getString("image_path"), rs.getString("description")),
                prefecture, limit);
    }

//  BLOB = float32 리틀엔디언을 이어 붙인 것 (embed_places.py 의 struct.pack('<1024f'))
    private static float[] toVector(byte[] bytes) {
        FloatBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
        float[] vector = new float[buffer.remaining()];
        buffer.get(vector);
        return vector;
    }
}
