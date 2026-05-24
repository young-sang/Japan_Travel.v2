package com.japantravel.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class AuditLogRepository {

    private final JdbcTemplate jdbc;

    public AuditLogRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void insert(Long userId, String username, String action,
                       String targetType, Long targetId, String detail) {
        jdbc.update(
                "INSERT INTO audit_log(user_id, username, action, target_type, target_id, detail) VALUES (?,?,?,?,?,?)",
                userId, username, action, targetType, targetId, detail);
    }

    public List<Map<String, Object>> listFiltered(Long userId, String action,
                                                  String from, String to,
                                                  int limit, int offset) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, user_id, username, action, target_type, target_id, detail, created_at FROM audit_log WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (userId != null) { sql.append(" AND user_id=?"); args.add(userId); }
        if (action != null && !action.isBlank()) { sql.append(" AND action=?"); args.add(action); }
        if (from != null && !from.isBlank()) { sql.append(" AND created_at >= ?"); args.add(from); }
        if (to != null && !to.isBlank()) { sql.append(" AND created_at <= ?"); args.add(to); }
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);
        return jdbc.queryForList(sql.toString(), args.toArray());
    }

    public int count(Long userId, String action, String from, String to) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_log WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (userId != null) { sql.append(" AND user_id=?"); args.add(userId); }
        if (action != null && !action.isBlank()) { sql.append(" AND action=?"); args.add(action); }
        if (from != null && !from.isBlank()) { sql.append(" AND created_at >= ?"); args.add(from); }
        if (to != null && !to.isBlank()) { sql.append(" AND created_at <= ?"); args.add(to); }
        Integer n = jdbc.queryForObject(sql.toString(), Integer.class, args.toArray());
        return n == null ? 0 : n;
    }
}
