package com.japantravel.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

//  테스트 DB 에 스키마와 시드가 들어갔는지만 본다. 이게 깨지면 나머지 통합 테스트는 볼 필요가 없다.
class SmokeContextTest extends IntegrationTest {

    @Test
    void 시드가_들어가_있다() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM prefectures", Long.class)).isEqualTo(47);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM destinations", Long.class)).isPositive();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM festivals", Long.class)).isPositive();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM courses WHERE owner_user_id IS NULL", Long.class)).isPositive();
    }
}
