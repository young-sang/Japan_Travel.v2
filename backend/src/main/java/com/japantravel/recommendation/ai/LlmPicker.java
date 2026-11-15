package com.japantravel.recommendation.ai;

import java.util.List;

//  AI 추천 페이지의 LLM 호출 (M4). 테스트는 이 인터페이스를 @MockBean 으로 바꿔 실제로 부르지 않는다
//  고른 key 가 후보 안에 있는지는 여기서 믿지 않는다 — AiRecommendationService 가 검증한다
public interface LlmPicker {

//  key = 종류 + id ("D123" 여행지, "F45" 축제). 여행지와 축제는 id 가 겹치므로 숫자만 쓰지 않는다 (M4-2)
//  kind = 프롬프트에 보일 한글 종류 ("여행지" · "축제")
    record Candidate(String key, String kind, String name, String description) {
    }

    record Pick(String key, String reason) {
    }

//  season = 프롬프트에 넣을 방문 시기 (예: "가을(9~11월)"), 상관없음이면 null
//  실패(키 없음 · 네트워크 · 타임아웃 · 응답 형식)는 ApiException(AI_RECOMMEND_FAILED)
    List<Pick> pick(String preference, String season, List<Candidate> candidates);
}
