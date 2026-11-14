package com.japantravel.recommendation.service;

import com.japantravel.recommendation.dto.ActivityKind;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

//  선정 이유 문장 · 조사 와/과 (D-100). 스프링 없이 돈다
class RecommendationReasonTest {

    @Test
    void 받침이_없으면_와_있으면_과() {
        assertThat(RecommendationReason.withOrGwa("도지")).isEqualTo("와");
        assertThat(RecommendationReason.withOrGwa("오타루 운하")).isEqualTo("와");
        assertThat(RecommendationReason.withOrGwa("오사카성")).isEqualTo("과");
        assertThat(RecommendationReason.withOrGwa("니시키 시장")).isEqualTo("과");
    }

    @Test
    void 한글이_아닌_글자로_끝나면_그_앞의_한글을_본다() {
        assertThat(RecommendationReason.withOrGwa("히카와 신사 (氷川神社)")).isEqualTo("와");
        assertThat(RecommendationReason.withOrGwa("도쿄 타워 2")).isEqualTo("와");
        assertThat(RecommendationReason.withOrGwa("ABC")).isEqualTo("와");
    }

    @Test
    void 활동_종류마다_문장이_다르다() {
        assertThat(RecommendationReason.write(ActivityKind.FAVORITE, "오타루 운하")).isEqualTo("즐겨찾기한 '오타루 운하'와 비슷한 곳이에요");
        assertThat(RecommendationReason.write(ActivityKind.REVIEW, "니시키 시장")).isEqualTo("리뷰를 남긴 '니시키 시장'과 비슷한 곳이에요");
        assertThat(RecommendationReason.write(ActivityKind.HISTORY, "킨카쿠지")).isEqualTo("최근 본 '킨카쿠지'와 비슷한 곳이에요");
    }
}
