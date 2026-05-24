package com.japantravel.repository;

import com.japantravel.dto.Dtos.BulkRun;
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
public class BulkRunRepository {
    private final JdbcTemplate jdbc;
    public BulkRunRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<BulkRun> row = (rs, i) -> new BulkRun(
            rs.getLong("id"),
            rs.getString("started_at"),
            rs.getString("finished_at"),
            rs.getString("status"),
            rs.getInt("total_tasks"),
            rs.getInt("tasks_success"),
            rs.getInt("tasks_partial"),
            rs.getInt("tasks_failed"),
            rs.getInt("tasks_aborted"),
            rs.getString("notes")
    );

    /** 새 bulk_runs row 생성. status='running', started_at=now. return id. */
    public long create(int totalTasks) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO bulk_runs(started_at, status, total_tasks) " +
                            "VALUES (datetime('now'), 'running', ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, totalTasks);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    /**
     * child run 1건 종료 시 호출. childStatus 별로 카운터 +1.
     * empty는 success로 카운트.
     */
    public void incrementChildResult(long bulkRunId, String childStatus) {
        String col = switch (childStatus == null ? "" : childStatus) {
            case "success", "empty" -> "tasks_success";
            case "partial" -> "tasks_partial";
            case "failed" -> "tasks_failed";
            case "aborted" -> "tasks_aborted";
            default -> null;
        };
        if (col == null) return;
        jdbc.update("UPDATE bulk_runs SET " + col + " = " + col + " + 1 WHERE id=?", bulkRunId);
    }

    /**
     * 해당 bulk_run의 모든 child가 종료 상태면 부모를 finalize.
     * - 모두 success → 'success'
     * - 하나라도 failed/aborted 있고 success도 있음 → 'partial'
     * - 전부 failed/aborted → 'failed'
     * - empty 섞임 → success로 카운트
     */
    public void finalizeIfDone(long bulkRunId) {
        Integer pending = jdbc.queryForObject(
                "SELECT COUNT(*) FROM collector_runs WHERE bulk_run_id=? AND status NOT IN " +
                        "('success','partial','failed','aborted','empty')",
                Integer.class, bulkRunId);
        if (pending == null || pending > 0) return;

        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM collector_runs WHERE bulk_run_id=?",
                Integer.class, bulkRunId);
        if (total == null || total == 0) return;

        Integer successCnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM collector_runs WHERE bulk_run_id=? AND status IN ('success','empty')",
                Integer.class, bulkRunId);
        Integer badCnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM collector_runs WHERE bulk_run_id=? AND status IN ('failed','aborted')",
                Integer.class, bulkRunId);

        int s = successCnt == null ? 0 : successCnt;
        int b = badCnt == null ? 0 : badCnt;

        String parentStatus;
        if (b == 0) parentStatus = "success";
        else if (s == 0) parentStatus = "failed";
        else parentStatus = "partial";

        jdbc.update(
                "UPDATE bulk_runs SET status=?, finished_at=datetime('now') WHERE id=? AND finished_at IS NULL",
                parentStatus, bulkRunId);
    }

    /** 백엔드 재기동으로 멈춘 'running' bulk_runs를 'aborted'로 마킹. */
    public int cleanupZombies() {
        return jdbc.update(
                "UPDATE bulk_runs SET status='aborted', finished_at=datetime('now') " +
                "WHERE status='running' AND finished_at IS NULL");
    }

    public Optional<BulkRun> findRunning() {
        var list = jdbc.query(
                "SELECT * FROM bulk_runs WHERE status='running' ORDER BY id DESC LIMIT 1", row);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<BulkRun> recent(int limit) {
        return jdbc.query(
                "SELECT * FROM bulk_runs ORDER BY finished_at DESC, started_at DESC LIMIT ?",
                row, limit);
    }

    public Optional<BulkRun> findById(long id) {
        var list = jdbc.query("SELECT * FROM bulk_runs WHERE id=?", row, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
