package com.japantravel.repository;

import com.japantravel.dto.Dtos.CollectorRunStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
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
            rs.getString("started_at"), rs.getString("finished_at"),
            rs.getObject("bulk_run_id") == null ? null : rs.getLong("bulk_run_id"),
            rs.getString("last_heartbeat_at"),
            rs.getString("stage")
    );

    /** 기존 시그니처 보존 — bulk_run_id 없이 단일 실행용. */
    public long start(String type, String prefecture, String source) {
        return start(type, prefecture, source, null);
    }

    /** bulk_run_id를 함께 기록하는 오버로드. nullable. */
    public long start(String type, String prefecture, String source, Long bulkRunId) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO collector_runs(type, prefecture, source, status, bulk_run_id) " +
                            "VALUES (?,?,?, 'queued', ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, type);
            ps.setString(2, prefecture);
            ps.setString(3, source);
            if (bulkRunId == null) ps.setNull(4, Types.INTEGER);
            else ps.setLong(4, bulkRunId);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public void markRunning(long id) {
        jdbc.update("UPDATE collector_runs SET status='running', last_heartbeat_at=datetime('now') " +
                "WHERE id=? AND status='queued'", id);
    }

    /** 좀비 정리: 백엔드 재기동으로 멈춘 'running'/'queued' row를 'aborted'로 마킹. */
    public int cleanupZombies() {
        return jdbc.update(
                "UPDATE collector_runs SET status='aborted', finished_at=datetime('now') " +
                "WHERE status IN ('running','queued') AND finished_at IS NULL");
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

    /**
     * 멱등 finish: finished_at이 NULL일 때만 UPDATE.
     * timeout 경로와 processGuarded finally 경로가 경합해도 한 번만 실제 갱신.
     * @return 1이면 이 호출이 실제로 finalize했음, 0이면 이미 끝나 있었음.
     */
    public int finishIfNotDone(long id, String status, String errorsJson) {
        return jdbc.update(
                "UPDATE collector_runs SET status=?, finished_at=datetime('now'), errors_json=? " +
                        "WHERE id=? AND finished_at IS NULL",
                status, errorsJson, id);
    }

    public void heartbeat(long runId) {
        jdbc.update("UPDATE collector_runs SET last_heartbeat_at=datetime('now') WHERE id=?", runId);
    }

    public void setStage(long runId, String stage) {
        jdbc.update("UPDATE collector_runs SET stage=? WHERE id=?", stage, runId);
    }

    /**
     * heartbeat가 threshold초보다 오래된 running row 또는 heartbeat 없이 started_at이 오래된 row.
     */
    public List<CollectorRunStatus> findStaleRunning(int thresholdSeconds) {
        String cutoff = "datetime('now', '-" + thresholdSeconds + " seconds')";
        return jdbc.query(
                "SELECT * FROM collector_runs WHERE status='running' AND (" +
                        "  (last_heartbeat_at IS NOT NULL AND last_heartbeat_at < " + cutoff + ")" +
                        "  OR (last_heartbeat_at IS NULL AND started_at < " + cutoff + ")" +
                        ")",
                row);
    }

    public List<CollectorRunStatus> findByBulkRunId(long bulkRunId) {
        return jdbc.query("SELECT * FROM collector_runs WHERE bulk_run_id=? ORDER BY id", row, bulkRunId);
    }

    /** errors_json 컬럼만 단건 조회. row가 없거나 NULL이면 빈 Optional. */
    public Optional<String> findErrorsJson(long id) {
        var list = jdbc.query("SELECT errors_json FROM collector_runs WHERE id=?",
                (rs, i) -> rs.getString(1), id);
        if (list.isEmpty()) return Optional.empty();
        String s = list.get(0);
        return s == null ? Optional.empty() : Optional.of(s);
    }

    public Optional<CollectorRunStatus> find(long id) {
        var list = jdbc.query("SELECT * FROM collector_runs WHERE id=?", row, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<CollectorRunStatus> recent(int limit) {
        return jdbc.query("SELECT * FROM collector_runs ORDER BY id DESC LIMIT ?", row, limit);
    }
}
