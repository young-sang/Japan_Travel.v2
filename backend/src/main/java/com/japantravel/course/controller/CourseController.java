package com.japantravel.course.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.course.dto.CourseRequest;
import com.japantravel.course.dto.CourseResponse;
import com.japantravel.course.dto.CourseSummaryResponse;
import com.japantravel.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//  /api/courses 와 /api/me/courses (D-045) 두 접두사를 쓰므로 클래스 수준 @RequestMapping 을 두지 않는다
//  (review 와 같은 방식). GET /api/courses/** 는 공개, 나머지는 로그인 필요 — SecurityConfig.
@RestController
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/api/courses")
    public ApiResponse<List<CourseSummaryResponse>> list(@RequestParam(required = false) String prefecture) {
        return ApiResponse.ok(courseService.findPublic(prefecture));
    }

//  공개 경로지만 토큰이 있으면 JwtAuthenticationFilter 가 인증을 채운다 — 비공개 코스의 주인을 알아보는 데 쓴다.
//  토큰이 없으면 userId 는 null.
    @GetMapping("/api/courses/{id}")
    public ApiResponse<CourseResponse> detail(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return ApiResponse.ok(courseService.findById(id, userId));
    }

    @PostMapping("/api/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CourseResponse> create(@AuthenticationPrincipal Long userId, @RequestBody CourseRequest req) {
        return ApiResponse.ok(courseService.create(userId, req));
    }

    @PutMapping("/api/courses/{id}")
    public ApiResponse<CourseResponse> update(@AuthenticationPrincipal Long userId,
                                              @PathVariable Long id,
                                              @RequestBody CourseRequest req) {
        return ApiResponse.ok(courseService.update(userId, id, req));
    }

    @DeleteMapping("/api/courses/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        courseService.delete(userId, id);
        return ApiResponse.ok();
    }

    @GetMapping("/api/me/courses")
    public ApiResponse<List<CourseSummaryResponse>> mine(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(courseService.findMine(userId));
    }
}
