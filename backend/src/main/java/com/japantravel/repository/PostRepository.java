package com.japantravel.repository;

import com.japantravel.dto.Dtos.Post;
import com.japantravel.dto.Dtos.PostComment;
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
public class PostRepository {

    private final JdbcTemplate jdbc;
    public PostRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Post> postRow = (rs, i) -> new Post(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("user_name"),
            rs.getString("title"),
            rs.getString("body"),
            rs.getString("created_at"),
            rs.getString("updated_at"),
            rs.getInt("comment_count")
    );

    private final RowMapper<PostComment> commentRow = (rs, i) -> new PostComment(
            rs.getLong("id"),
            rs.getLong("post_id"),
            rs.getLong("user_id"),
            rs.getString("user_name"),
            rs.getString("body"),
            rs.getString("created_at")
    );

    private static final String SELECT_POST = """
            SELECT p.id, p.user_id, u.nickname AS user_name,
                   p.title, p.body, p.created_at, p.updated_at,
                   (SELECT COUNT(*) FROM post_comments c WHERE c.post_id = p.id) AS comment_count
              FROM posts p
              LEFT JOIN users u ON u.id = p.user_id
            """;

    public List<Post> listPaged(int offset, int size) {
        return jdbc.query(SELECT_POST + " ORDER BY p.created_at DESC LIMIT ? OFFSET ?",
                postRow, size, offset);
    }

    public int count() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM posts", Integer.class);
        return n == null ? 0 : n;
    }

    public Optional<Post> findById(Long id) {
        return jdbc.query(SELECT_POST + " WHERE p.id = ?", postRow, id).stream().findFirst();
    }

    public Long insert(Long userId, String title, String body) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO posts(user_id, title, body) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, title);
            ps.setString(3, body);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public boolean update(Long id, Long userId, String title, String body) {
        int n = jdbc.update(
                "UPDATE posts SET title=?, body=?, updated_at=CURRENT_TIMESTAMP WHERE id=? AND user_id=?",
                title, body, id, userId);
        return n > 0;
    }

    public boolean delete(Long id, Long userId) {
        int n = jdbc.update("DELETE FROM posts WHERE id=? AND user_id=?", id, userId);
        return n > 0;
    }

    public boolean deleteByAdmin(Long id) {
        int n = jdbc.update("DELETE FROM posts WHERE id=?", id);
        return n > 0;
    }

    private static final String SELECT_COMMENT = """
            SELECT c.id, c.post_id, c.user_id, u.nickname AS user_name,
                   c.body, c.created_at
              FROM post_comments c
              LEFT JOIN users u ON u.id = c.user_id
            """;

    public List<PostComment> listComments(Long postId) {
        return jdbc.query(SELECT_COMMENT + " WHERE c.post_id = ? ORDER BY c.created_at ASC",
                commentRow, postId);
    }

    public Long insertComment(Long postId, Long userId, String body) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO post_comments(post_id, user_id, body) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, postId);
            ps.setLong(2, userId);
            ps.setString(3, body);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public boolean deleteComment(Long id, Long userId) {
        int n = jdbc.update("DELETE FROM post_comments WHERE id=? AND user_id=?", id, userId);
        return n > 0;
    }

    public boolean deleteCommentByAdmin(Long id) {
        int n = jdbc.update("DELETE FROM post_comments WHERE id=?", id);
        return n > 0;
    }
}
