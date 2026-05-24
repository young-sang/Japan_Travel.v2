package com.japantravel.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.japantravel.dto.Dtos.Course;
import com.japantravel.dto.Dtos.CourseStop;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CourseRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public CourseRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Course> rowMapper = (rs, i) -> {
        List<String> tags;
        List<CourseStop> timeline;
        try {
            tags = mapper.readValue(rs.getString("tags"), new TypeReference<List<String>>(){});
            timeline = mapper.readValue(rs.getString("timeline_json"), new TypeReference<List<CourseStop>>(){});
        } catch (Exception e) { tags = List.of(); timeline = List.of(); }
        return new Course(
                rs.getLong("id"), rs.getString("title"), rs.getString("prefecture"),
                tags, rs.getString("duration"), rs.getString("image_path"),
                (Double) rs.getObject("center_lat"), (Double) rs.getObject("center_lng"),
                timeline, rs.getInt("is_user_created") == 1,
                (Long) rs.getObject("owner_user_id"),
                rs.getString("status"), rs.getString("created_at")
        );
    };

    public List<Course> findAll(String prefecture, String tag, Long ownerUserId, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM courses WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (prefecture != null && !prefecture.isBlank()) { sql.append(" AND prefecture = ?"); args.add(prefecture); }
        if (tag != null && !tag.isBlank()) { sql.append(" AND tags LIKE ?"); args.add("%\"" + tag + "\"%"); }
        if (ownerUserId != null) { sql.append(" AND owner_user_id = ?"); args.add(ownerUserId); }
        if (status != null && !status.isBlank()) { sql.append(" AND status = ?"); args.add(status); }
        sql.append(" ORDER BY id DESC");
        return jdbc.query(sql.toString(), rowMapper, args.toArray());
    }

    public Optional<Course> findById(long id) {
        var list = jdbc.query("SELECT * FROM courses WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public long create(String title, String prefecture, List<String> tags, String duration,
                       String imagePath, Double centerLat, Double centerLng,
                       List<CourseStop> timeline, boolean userCreated, Long ownerUserId, String status) {
        String tagsJson, timelineJson;
        try {
            tagsJson = mapper.writeValueAsString(tags);
            timelineJson = mapper.writeValueAsString(timeline);
        } catch (Exception e) { tagsJson = "[]"; timelineJson = "[]"; }
        KeyHolder kh = new GeneratedKeyHolder();
        final String tj = tagsJson, tlj = timelineJson;
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                INSERT INTO courses(title, prefecture, tags, duration, image_path, center_lat, center_lng,
                                    timeline_json, is_user_created, owner_user_id, status)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, title); ps.setString(2, prefecture); ps.setString(3, tj);
            ps.setString(4, duration); ps.setString(5, imagePath);
            if (centerLat == null) ps.setNull(6, java.sql.Types.DOUBLE); else ps.setDouble(6, centerLat);
            if (centerLng == null) ps.setNull(7, java.sql.Types.DOUBLE); else ps.setDouble(7, centerLng);
            ps.setString(8, tlj);
            ps.setInt(9, userCreated ? 1 : 0);
            if (ownerUserId == null) ps.setNull(10, java.sql.Types.BIGINT); else ps.setLong(10, ownerUserId);
            ps.setString(11, status);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public void update(long id, String title, String prefecture, List<String> tags, String duration,
                       String imagePath, Double centerLat, Double centerLng,
                       List<CourseStop> timeline, String status) {
        String tagsJson, timelineJson;
        try {
            tagsJson = mapper.writeValueAsString(tags);
            timelineJson = mapper.writeValueAsString(timeline);
        } catch (Exception e) { tagsJson = "[]"; timelineJson = "[]"; }
        jdbc.update("""
            UPDATE courses SET title=?, prefecture=?, tags=?, duration=?, image_path=?,
                   center_lat=?, center_lng=?, timeline_json=?, status=?
             WHERE id=?
            """, title, prefecture, tagsJson, duration, imagePath, centerLat, centerLng, timelineJson, status, id);
    }

    public void delete(long id) { jdbc.update("DELETE FROM courses WHERE id = ?", id); }

    public int count() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM courses", Integer.class);
        return n == null ? 0 : n;
    }
}
