package com.japantravel.repository;

import com.japantravel.dto.Dtos.CollectorRunStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class CollectorRunRepository {
    private final JdbcTemplate jdbc;
    public CollectorRunRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<CollectorRunStatus> row = (rs, i) -> new CollectorRunStatus(
            rs.getLong("id"), rs.getString("type"), rs.getString("prefecture"),
            rs.getString("source"), rs.getString("status"),
            rs.getInt("items_added"), rs.getInt("items_updated"), rs.getInt("items_failed"),
            rs.getString("started_at"), rs.getString("finished_at")
    );

    public long start(String type, String prefecture, String source) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO collector_runs(type, prefecture, source, status) VALUES (?,?,?, 'running')",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, type); ps.setString(2, prefecture); ps.setString(3, source);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public void increment(long id, int added, int updated, int failed) {
        jdbc.update("""
            UPDATE collector_runs SET items_added = items_added + ?, items_updated = items_updated + ?,
                   items_failed = items_failed + ? WHERE id = ?
            """, added, updated, failed, id);
    }

    public void finish(long id, String status, String errorsJson) {
        jdbc.update("UPDATE collector_runs SET status=?, finished_at=datetime('now'), errors_json=? WHERE id=?",
                status, errorsJson, id);
    }

    public Optional<CollectorRunStatus> find(long id) {
        var list = jdbc.query("SELECT * FROM collector_runs WHERE id=?", row, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<CollectorRunStatus> recent(int limit) {
        return jdbc.query("SELECT * FROM collector_runs ORDER BY id DESC LIMIT ?", row, limit);
    }
}
