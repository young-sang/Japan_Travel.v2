package com.japantravel.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.japantravel.dto.Dtos.Destination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DestinationRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public DestinationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Destination> rowMapper = (rs, i) -> {
        List<String> tags;
        try {
            tags = mapper.readValue(rs.getString("tags"), new TypeReference<List<String>>(){});
        } catch (Exception e) {
            tags = List.of();
        }
        Double lat = (Double) rs.getObject("lat");
        Double lng = (Double) rs.getObject("lng");
        return new Destination(
                rs.getLong("id"), rs.getString("name"), rs.getString("prefecture"),
                tags, lat, lng, rs.getString("image_path"), rs.getString("description"),
                rs.getString("wiki_title"), rs.getString("last_refreshed_at")
        );
    };

    public List<Destination> findAll(String prefecture, String tag) {
        StringBuilder sql = new StringBuilder("SELECT * FROM destinations WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (prefecture != null && !prefecture.isBlank()) {
            sql.append(" AND prefecture = ?");
            args.add(prefecture);
        }
        if (tag != null && !tag.isBlank()) {
            sql.append(" AND tags LIKE ?");
            args.add("%\"" + tag + "\"%");
        }
        sql.append(" ORDER BY id DESC");
        return jdbc.query(sql.toString(), rowMapper, args.toArray());
    }

    public Optional<Destination> findById(long id) {
        var list = jdbc.query("SELECT * FROM destinations WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Destination> findByWikiTitle(String title) {
        var list = jdbc.query("SELECT * FROM destinations WHERE wiki_title = ?", rowMapper, title);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** Wikipedia title 기준 UPSERT. 새로 추가 시 true. */
    public boolean upsertByWiki(String name, String prefecture, List<String> tags,
                                Double lat, Double lng, String imagePath,
                                String description, String wikiTitle) {
        String tagsJson;
        try { tagsJson = mapper.writeValueAsString(tags); } catch (Exception e) { tagsJson = "[]"; }
        var existing = findByWikiTitle(wikiTitle);
        if (existing.isPresent()) {
            jdbc.update("""
                UPDATE destinations
                   SET name=?, prefecture=?, tags=?, lat=?, lng=?, image_path=?, description=?,
                       last_refreshed_at=datetime('now')
                 WHERE wiki_title=?
                """, name, prefecture, tagsJson, lat, lng, imagePath, description, wikiTitle);
            return false;
        } else {
            jdbc.update("""
                INSERT INTO destinations(name, prefecture, tags, lat, lng, image_path, description, wiki_title, last_refreshed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))
                """, name, prefecture, tagsJson, lat, lng, imagePath, description, wikiTitle);
            return true;
        }
    }

    public int count() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM destinations", Integer.class);
        return n == null ? 0 : n;
    }

    public List<String> distinctPrefectures() {
        return jdbc.query("SELECT DISTINCT prefecture FROM destinations ORDER BY prefecture", (rs, i) -> rs.getString(1));
    }
}
