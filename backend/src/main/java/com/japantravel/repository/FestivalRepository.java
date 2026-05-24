package com.japantravel.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.japantravel.dto.Dtos.Festival;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class FestivalRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public FestivalRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Festival> rowMapper = (rs, i) -> new Festival(
            rs.getLong("id"), rs.getString("name"), rs.getString("prefecture"),
            (Integer) rs.getObject("month"), rs.getString("date_text"),
            rs.getString("image_path"), rs.getString("description"), rs.getString("wiki_title"),
            (Double) rs.getObject("lat"), (Double) rs.getObject("lng"),
            rs.getString("last_refreshed_at")
    );

    public List<Festival> findAll(String prefecture, Integer month) {
        StringBuilder sql = new StringBuilder("SELECT * FROM festivals WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (prefecture != null && !prefecture.isBlank()) {
            sql.append(" AND prefecture = ?"); args.add(prefecture);
        }
        if (month != null) {
            sql.append(" AND month = ?"); args.add(month);
        }
        sql.append(" ORDER BY month, id");
        return jdbc.query(sql.toString(), rowMapper, args.toArray());
    }

    public Optional<Festival> findById(long id) {
        var list = jdbc.query("SELECT * FROM festivals WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Festival> findByWikiTitle(String title) {
        var list = jdbc.query("SELECT * FROM festivals WHERE wiki_title = ?", rowMapper, title);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean upsertByWiki(String name, String prefecture, Integer month, String dateText,
                                Double lat, Double lng, String imagePath, String description,
                                String wikiTitle) {
        var existing = findByWikiTitle(wikiTitle);
        if (existing.isPresent()) {
            jdbc.update("""
                UPDATE festivals SET name=?, prefecture=?, month=?, date_text=?, lat=?, lng=?,
                       image_path=?, description=?, last_refreshed_at=datetime('now')
                 WHERE wiki_title=?
                """, name, prefecture, month, dateText, lat, lng, imagePath, description, wikiTitle);
            return false;
        }
        jdbc.update("""
            INSERT INTO festivals(name, prefecture, month, date_text, lat, lng, image_path, description, wiki_title, last_refreshed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
            """, name, prefecture, month, dateText, lat, lng, imagePath, description, wikiTitle);
        return true;
    }

    public int count() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM festivals", Integer.class);
        return n == null ? 0 : n;
    }

    public java.util.Map<String, Integer> countsByPrefecture() {
        java.util.Map<String, Integer> out = new java.util.HashMap<>();
        jdbc.query("SELECT prefecture, COUNT(*) AS n FROM festivals GROUP BY prefecture", rs -> {
            out.put(rs.getString("prefecture"), rs.getInt("n"));
        });
        return out;
    }

    // ── manual CRUD (admin) ────────────────────────────────────────────────
    public long insert(Festival f) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                INSERT INTO festivals(name, prefecture, month, date_text, lat, lng, image_path, description, wiki_title, last_refreshed_at)
                VALUES (?,?,?,?,?,?,?,?,?, datetime('now'))
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, f.name());
            ps.setString(2, f.prefecture());
            if (f.month() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, f.month());
            ps.setString(4, f.dateText());
            if (f.lat() == null) ps.setNull(5, Types.DOUBLE); else ps.setDouble(5, f.lat());
            if (f.lng() == null) ps.setNull(6, Types.DOUBLE); else ps.setDouble(6, f.lng());
            ps.setString(7, f.imagePath());
            ps.setString(8, f.description());
            ps.setString(9, f.wikiTitle()); // nullable for manual
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public void update(long id, Festival f) {
        jdbc.update("""
            UPDATE festivals
               SET name=?, prefecture=?, month=?, date_text=?, lat=?, lng=?,
                   image_path=?, description=?, wiki_title=?, last_refreshed_at=datetime('now')
             WHERE id=?
            """, f.name(), f.prefecture(), f.month(), f.dateText(),
                f.lat(), f.lng(), f.imagePath(), f.description(), f.wikiTitle(), id);
    }

    public void deleteById(long id) {
        jdbc.update("DELETE FROM favorites WHERE target_type='festival' AND target_id=?", id);
        jdbc.update("DELETE FROM reviews WHERE target_type='festival' AND target_id=?", id);
        jdbc.update("DELETE FROM history WHERE target_type='festival' AND target_id=?", id);
        jdbc.update("DELETE FROM festivals WHERE id=?", id);
    }
}
