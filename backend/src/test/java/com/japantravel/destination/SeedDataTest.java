package com.japantravel.destination;

import com.japantravel.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

//  시드 데이터 검사 (D-089 · D-090). 여행지는 Wikidata + ja 위키 원문 요약이고,
//  기본 코스 정류장은 장소를 이름으로 찾으므로 이름이 어긋나면 조용히 빠진다 (D-051) — 그걸 여기서 잡는다
class SeedDataTest extends IntegrationTest {

    @Test
    void 모든_여행지에_설명_출처가_있다() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM destinations", Integer.class);
        Integer withoutSource = jdbc.queryForObject(
                "SELECT COUNT(*) FROM destinations WHERE wikidata_id IS NULL OR source_url IS NULL", Integer.class);

        assertThat(total).isGreaterThan(900);
        assertThat(withoutSource).isZero();
    }

//  축제도 ja 위키 원문 요약이다 (D-092). Wikidata 항목이 없는 축제가 있어 wikidata_id 는 검사하지 않는다
    @Test
    void 모든_축제에_설명_출처와_개최_달이_있다() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM festivals", Integer.class);
        Integer withoutSource = jdbc.queryForObject(
                "SELECT COUNT(*) FROM festivals WHERE source_url IS NULL OR date_text IS NULL", Integer.class);

        assertThat(total).isGreaterThan(300);
        assertThat(withoutSource).isZero();
    }

    @Test
    void 기본_코스_정류장이_하나도_빠지지_않는다() throws Exception {
        String sql = new ClassPathResource("data.sql").getContentAsString(StandardCharsets.UTF_8);
        String stops = sql.substring(sql.indexOf("INSERT IGNORE INTO course_stops"));
        long expected = Pattern.compile(", '[DF]'(?: AS kind)?, '").matcher(stops).results().count();

        Integer actual = jdbc.queryForObject("""
                SELECT COUNT(*) FROM course_stops s JOIN courses c ON c.id = s.course_id
                WHERE c.owner_user_id IS NULL""", Integer.class);

        assertThat(expected).isPositive();
        assertThat(actual).isEqualTo((int) expected);
    }
}
