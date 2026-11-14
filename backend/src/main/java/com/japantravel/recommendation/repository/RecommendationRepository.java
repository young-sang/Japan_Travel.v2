package com.japantravel.recommendation.repository;

import com.japantravel.recommendation.dto.ActivityKind;
import com.japantravel.recommendation.dto.RecommendationTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

//  임베딩 테이블에는 엔티티가 없다 (읽기 전용 BLOB, D-096). SQL 두 개라 JdbcTemplate 으로 읽는다 (D-098)
@Repository
@RequiredArgsConstructor
public class RecommendationRepository {

    public record Activity(RecommendationTargetType type, long placeId, ActivityKind kind) {
    }

    public record Place(RecommendationTargetType type, long id, String name, String prefecture,
                        String imagePath, float[] vector) {
    }

    private final JdbcTemplate jdbc;

//  사용자가 활동한 장소. 리뷰는 한 장소에 여러 개여도 한 번만 센다 (DISTINCT)
    public List<Activity> findActivities(long userId) {
        return jdbc.query("""
                SELECT 'DESTINATION' AS type, destination_id AS place_id, 'FAVORITE' AS kind FROM favorite_destinations WHERE user_id = ?
                UNION ALL SELECT 'FESTIVAL', festival_id, 'FAVORITE' FROM favorite_festivals WHERE user_id = ?
                UNION ALL SELECT DISTINCT 'DESTINATION', destination_id, 'REVIEW' FROM review_destinations WHERE user_id = ?
                UNION ALL SELECT DISTINCT 'FESTIVAL', festival_id, 'REVIEW' FROM review_festivals WHERE user_id = ?
                UNION ALL SELECT 'DESTINATION', destination_id, 'HISTORY' FROM history_destinations WHERE user_id = ?
                UNION ALL SELECT 'FESTIVAL', festival_id, 'HISTORY' FROM history_festivals WHERE user_id = ?
                """,
                (rs, i) -> new Activity(RecommendationTargetType.valueOf(rs.getString("type")),
                        rs.getLong("place_id"), ActivityKind.valueOf(rs.getString("kind"))),
                userId, userId, userId, userId, userId, userId);
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

//  BLOB = float32 리틀엔디언을 이어 붙인 것 (embed_places.py 의 struct.pack('<1024f'))
    private static float[] toVector(byte[] bytes) {
        FloatBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
        float[] vector = new float[buffer.remaining()];
        buffer.get(vector);
        return vector;
    }
}
