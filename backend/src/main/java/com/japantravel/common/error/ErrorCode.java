package com.japantravel.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

//  이 API 가 낼 수 있는 모든 에러. 각 코드가 어디서 · 무엇 때문에 나는지는 docs/ERRORS.md.
//  추가 규칙: 그 에러를 던지는 코드를 만들 때 함께 추가한다. 쓰는 곳 없는 코드는 미리 넣지 않는다.
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

//  공통
    PREFECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 현입니다"),

//  destination
    DESTINATION_NOT_FOUND(HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다"),

//  festival
    FESTIVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "축제를 찾을 수 없습니다"),
    INVALID_MONTH(HttpStatus.BAD_REQUEST, "month 는 1~12 여야 합니다"),

//  user
    USERNAME_TAKEN(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다"),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다"),
//  비밀번호 변경 · 탈퇴의 본인 확인 실패. 401 이 아니다 — 로그인은 유효하다 (D-056)
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다"),
//  남의 것을 건드림. 도메인 이름을 붙이지 않는다 — review · course 가 같이 쓴다
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),

//  review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다"),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "별점은 1~5 여야 합니다"),

//  course — 남의 비공개 코스도 COURSE_NOT_FOUND (존재를 숨긴다, D-047)
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "코스를 찾을 수 없습니다"),
//  본문 규칙 위반은 어느 필드든 이 코드 하나 (D-047)
    INVALID_COURSE(HttpStatus.BAD_REQUEST, "코스 요청 값이 올바르지 않습니다"),

//  news (D-079)
    NEWS_NOT_FOUND(HttpStatus.NOT_FOUND, "뉴스를 찾을 수 없습니다"),
//  제목 · 본문이 비었거나 제목이 200자를 넘음
    INVALID_NEWS(HttpStatus.BAD_REQUEST, "뉴스 요청 값이 올바르지 않습니다"),

//  AI 추천 페이지 (M4-1). 실패 상황별 문구는 M4-3 에서 나눈다
//  취향 문장이 비었거나 200자 초과, 또는 도도부현 없음
    INVALID_AI_REQUEST(HttpStatus.BAD_REQUEST, "취향 문장과 지역을 확인해 주세요"),
//  조건(도도부현 · 종류 · 계절)에 맞는 후보가 0곳 — LLM 을 부르지 않는다 (M4-3). 화면이 자기 입력으로 안내 문구를 만든다
    NO_AI_CANDIDATES(HttpStatus.NOT_FOUND, "조건에 맞는 장소가 없습니다"),
//  LLM 호출 실패 · 검증 후 남은 장소 없음. 둘은 서버 로그로만 구분한다 (M4-3)
    AI_RECOMMEND_FAILED(HttpStatus.BAD_GATEWAY, "AI 추천을 만들지 못했습니다. 잠시 후 다시 시도해 주세요"),

//  Spring 이 던지는 요청 오류 (ApiExceptionHandler 가 변환)
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 값의 형식이 올바르지 않습니다"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type 입니다"),
    API_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 API 입니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않는 HTTP 메서드입니다"),

//  그 밖의 모든 예외
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다");

    private final HttpStatus status;
    private final String message;
}
