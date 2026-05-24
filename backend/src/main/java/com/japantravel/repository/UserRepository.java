package com.japantravel.repository;

import com.japantravel.dto.Dtos.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;
    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<User> publicRow = (rs, i) -> new User(
            rs.getLong("id"), rs.getString("username"), rs.getString("nickname"),
            rs.getString("role"), rs.getString("created_at"));

    public Optional<User> findByUsername(String username) {
        List<User> list = jdbc.query(
                "SELECT id, username, nickname, role, created_at FROM users WHERE username=?",
                publicRow, username);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<User> findById(long id) {
        List<User> list = jdbc.query(
                "SELECT id, username, nickname, role, created_at FROM users WHERE id=?",
                publicRow, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<String> findPasswordHash(String username) {
        List<String> list = jdbc.query(
                "SELECT password_hash FROM users WHERE username=?",
                (rs, i) -> rs.getString(1), username);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean existsByUsername(String username) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username=?", Integer.class, username);
        return n != null && n > 0;
    }

    public long insert(String username, String passwordHash, String nickname, String role) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users(username, password_hash, nickname, role) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, nickname);
            ps.setString(4, role);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public long insertWithId(long id, String username, String passwordHash, String nickname, String role) {
        jdbc.update("INSERT INTO users(id, username, password_hash, nickname, role) VALUES (?,?,?,?,?)",
                id, username, passwordHash, nickname, role);
        return id;
    }

    public List<Map<String, Object>> listAll() {
        return jdbc.queryForList(
                "SELECT id, username, nickname, role, created_at FROM users ORDER BY id");
    }

    public void updateRole(long id, String role) {
        jdbc.update("UPDATE users SET role=? WHERE id=?", role, id);
    }

    public int countByRole(String role) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE role=?", Integer.class, role);
        return n == null ? 0 : n;
    }

    @Transactional
    public void deleteByIdCascade(long id) {
        jdbc.update("DELETE FROM favorites WHERE user_id=?", id);
        jdbc.update("DELETE FROM reviews WHERE user_id=?", id);
        jdbc.update("DELETE FROM courses WHERE owner_user_id=?", id);
        jdbc.update("DELETE FROM history WHERE user_id=?", id);
        jdbc.update("DELETE FROM audit_log WHERE user_id=?", id);
        jdbc.update("DELETE FROM users WHERE id=?", id);
    }
}
