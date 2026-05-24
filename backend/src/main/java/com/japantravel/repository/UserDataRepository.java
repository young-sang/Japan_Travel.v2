package com.japantravel.repository;

import com.japantravel.dto.Dtos.Favorite;
import com.japantravel.dto.Dtos.HistoryItem;
import com.japantravel.dto.Dtos.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/** favorites · reviews · history 통합 레포지토리. */
@Repository
public class UserDataRepository {

    private final JdbcTemplate jdbc;
    public UserDataRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    // ── favorites ──────────────────────────────────────────────────────────
    private final RowMapper<Favorite> favRow = (rs, i) -> new Favorite(
            rs.getString("target_type"), rs.getLong("target_id"), rs.getString("created_at"));

    public List<Favorite> listFavorites(long userId) {
        return jdbc.query("SELECT target_type, target_id, created_at FROM favorites WHERE user_id=? ORDER BY created_at DESC",
                favRow, userId);
    }

    public void addFavorite(long userId, String type, long targetId) {
        jdbc.update("INSERT OR IGNORE INTO favorites(user_id, target_type, target_id) VALUES (?,?,?)",
                userId, type, targetId);
    }

    public void removeFavorite(long userId, String type, long targetId) {
        jdbc.update("DELETE FROM favorites WHERE user_id=? AND target_type=? AND target_id=?",
                userId, type, targetId);
    }

    public int countFavorites(long userId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM favorites WHERE user_id=?", Integer.class, userId);
        return n == null ? 0 : n;
    }

    public int countAllFavorites() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM favorites", Integer.class);
        return n == null ? 0 : n;
    }

    public boolean reviewExistsForUser(long reviewId, long userId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM reviews WHERE id=? AND user_id=?",
                Integer.class, reviewId, userId);
        return n != null && n > 0;
    }

    // ── reviews ─────────────────────────────────────────────────────────────
    private final RowMapper<Review> reviewRow = (rs, i) -> new Review(
            rs.getLong("id"), rs.getLong("user_id"), rs.getString("target_type"), rs.getLong("target_id"),
            rs.getInt("rating"), rs.getString("comment"), rs.getString("created_at"));

    public List<Review> reviewsForTarget(String type, long targetId) {
        return jdbc.query("SELECT * FROM reviews WHERE target_type=? AND target_id=? ORDER BY id DESC",
                reviewRow, type, targetId);
    }

    public List<Review> reviewsByUser(long userId) {
        return jdbc.query("SELECT * FROM reviews WHERE user_id=? ORDER BY id DESC", reviewRow, userId);
    }

    public List<Review> allReviews() {
        return jdbc.query("SELECT * FROM reviews ORDER BY id DESC", reviewRow);
    }

    public long addReview(long userId, String type, long targetId, int rating, String comment) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO reviews(user_id, target_type, target_id, rating, comment) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId); ps.setString(2, type); ps.setLong(3, targetId);
            ps.setInt(4, rating); ps.setString(5, comment);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public void updateReview(long id, int rating, String comment) {
        jdbc.update("UPDATE reviews SET rating=?, comment=? WHERE id=?", rating, comment, id);
    }

    public void deleteReview(long id) {
        jdbc.update("DELETE FROM reviews WHERE id=?", id);
    }

    public double averageRating(String type, long targetId) {
        Double v = jdbc.queryForObject(
                "SELECT AVG(rating) FROM reviews WHERE target_type=? AND target_id=?",
                Double.class, type, targetId);
        return v == null ? 0.0 : v;
    }

    // ── history ─────────────────────────────────────────────────────────────
    private final RowMapper<HistoryItem> histRow = (rs, i) -> new HistoryItem(
            rs.getString("target_type"), rs.getLong("target_id"), rs.getString("visited_at"));

    public List<HistoryItem> historyOf(long userId) {
        return jdbc.query("SELECT * FROM history WHERE user_id=? ORDER BY visited_at DESC", histRow, userId);
    }

    public void touchHistory(long userId, String type, long targetId) {
        jdbc.update("""
            INSERT INTO history(user_id, target_type, target_id, visited_at)
            VALUES (?,?,?,datetime('now'))
            ON CONFLICT(user_id, target_type, target_id) DO UPDATE SET visited_at = datetime('now')
            """, userId, type, targetId);
    }

    public void clearHistory(long userId) {
        jdbc.update("DELETE FROM history WHERE user_id=?", userId);
    }

    // ── per-user counts (admin user mgmt) ──────────────────────────────────
    public int countFavoritesByUser(long userId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM favorites WHERE user_id=?", Integer.class, userId);
        return n == null ? 0 : n;
    }

    public int countReviewsByUser(long userId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM reviews WHERE user_id=?", Integer.class, userId);
        return n == null ? 0 : n;
    }

    public int countCoursesByUser(long userId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM courses WHERE owner_user_id=?", Integer.class, userId);
        return n == null ? 0 : n;
    }

    public int countHistoryByUser(long userId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM history WHERE user_id=?", Integer.class, userId);
        return n == null ? 0 : n;
    }
}
