package com.japantravel.recommendation.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

//  AI 추천 하루 요청 상한 (M4-4). 사이트 전체에서 LLM 을 실제로 부른 횟수만 센다.
//  날짜는 한국 시간 자정에 바뀐다. 횟수는 메모리에만 있어 서버를 재시작하면 0 이 된다 (기획에서 정함)
@Component
public class AiDailyLimiter {

    private final int limit;
    private final Clock clock;
    private LocalDate day;
    private int count;

    @Autowired
    public AiDailyLimiter(@Value("${llm.daily-limit}") int limit) {
        this(limit, Clock.system(ZoneId.of("Asia/Seoul")));
    }

//  테스트용 — 시계를 바꿔 날짜가 넘어가는 것을 확인한다
    AiDailyLimiter(int limit, Clock clock) {
        this.limit = limit;
        this.clock = clock;
    }

//  한 번 쓸 수 있으면 세고 true, 오늘 상한에 닿았으면 세지 않고 false
    public synchronized boolean tryAcquire() {
        LocalDate today = LocalDate.now(clock);
        if (!today.equals(day)) {
            day = today;
            count = 0;
        }
        if (count >= limit) {
            return false;
        }
        count++;
        return true;
    }
}
