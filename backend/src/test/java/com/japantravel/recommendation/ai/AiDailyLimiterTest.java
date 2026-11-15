package com.japantravel.recommendation.ai;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

//  AI 추천 하루 상한 (M4-4). 시계를 바꿔 한국 시간 자정에 다시 열리는지 본다
class AiDailyLimiterTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

//  테스트에서 앞으로 돌릴 수 있는 시계
    private static final class MovableClock extends Clock {
        private Instant now;

        MovableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneId getZone() {
            return SEOUL;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @Test
    void 상한까지만_허용한다() {
        AiDailyLimiter limiter = new AiDailyLimiter(2, new MovableClock(Instant.parse("2026-11-16T03:00:00Z")));
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isFalse();
        assertThat(limiter.tryAcquire()).isFalse();
    }

    @Test
    void 한국_시간_자정이_지나면_다시_쓸_수_있다() {
//      2026-11-16 23:30 KST
        MovableClock clock = new MovableClock(Instant.parse("2026-11-16T14:30:00Z"));
        AiDailyLimiter limiter = new AiDailyLimiter(1, clock);
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isFalse();

//      20분 뒤 = 23:50 KST, 아직 같은 날
        clock.advance(Duration.ofMinutes(20));
        assertThat(limiter.tryAcquire()).isFalse();

//      다시 20분 뒤 = 다음 날 00:10 KST
        clock.advance(Duration.ofMinutes(20));
        assertThat(limiter.tryAcquire()).isTrue();
        assertThat(limiter.tryAcquire()).isFalse();
    }
}
