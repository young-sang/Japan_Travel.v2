package com.japantravel.course.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.course.dto.CourseRequest;
import com.japantravel.course.dto.CourseResponse;
import com.japantravel.course.dto.CourseStopRequest;
import com.japantravel.course.dto.CourseSummaryResponse;
import com.japantravel.course.dto.StopType;
import com.japantravel.course.entity.Course;
import com.japantravel.course.entity.CourseStop;
import com.japantravel.course.repository.CourseRepository;
import com.japantravel.destination.entity.Destination;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.festival.entity.Festival;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.prefecture.entity.Prefecture;
import com.japantravel.prefecture.repository.PrefectureRepository;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//  수정 · 삭제의 판정 순서 (D-047): 본문 값 → 코스 존재 → 작성자 → 현 · 정류장 대상.
//  남의 비공개 코스는 403 이 아니라 404 — 상세 조회와 같이 존재를 숨긴다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final PrefectureRepository prefectureRepository;
    private final UserRepository userRepository;
//  정류장 대상 도메인은 존재 확인과 참조용으로만 읽는다
    private final DestinationRepository destinationRepository;
    private final FestivalRepository festivalRepository;

//  ---- 조회 ----

//  기본 제공 + 공개 사용자 코스. 로그인 여부와 무관하게 같은 결과 (D-045 · D-047).
    public List<CourseSummaryResponse> findPublic(String prefecture) {
        if (prefecture == null || prefecture.isBlank()) {
            return toSummaries(courseRepository.findByIsPublicTrueOrderByIdDesc());
        }
//      없는 현 이름은 404. 실재하는 현인데 0건이면 빈 목록 (destination 과 같은 규칙).
        if (!prefectureRepository.existsByName(prefecture)) {
            throw new ApiException(ErrorCode.PREFECTURE_NOT_FOUND);
        }
        return toSummaries(courseRepository.findByIsPublicTrueAndPrefectureNameOrderByIdDesc(prefecture));
    }

//  내 코스 — 공개 · 비공개 모두
    public List<CourseSummaryResponse> findMine(Long userId) {
        return toSummaries(courseRepository.findByOwnerIdOrderByIdDesc(userId));
    }

//  userId 는 비로그인이면 null
    public CourseResponse findById(Long id, Long userId) {
        return CourseResponse.from(findVisible(id, userId));
    }

//  ---- 쓰기 ----

    @Transactional
    public CourseResponse create(Long userId, CourseRequest req) {
        validate(req);
        Prefecture prefecture = findPrefecture(req.prefecture());
        List<ResolvedStop> stops = resolveStops(req.stops());

        Course course = new Course(req.title(), req.description(), prefecture, req.imagePath(),
                findUser(userId), req.isPublic());
        stops.forEach(s -> course.addStop(s.toEntity(course)));
        return CourseResponse.from(courseRepository.save(course));
    }

//  전체 덮어쓰기. 정류장은 전부 지우고 요청대로 다시 만든다.
    @Transactional
    public CourseResponse update(Long userId, Long id, CourseRequest req) {
        validate(req);
        Course course = findVisible(id, userId);
        checkOwner(course, userId);
        Prefecture prefecture = findPrefecture(req.prefecture());
        List<ResolvedStop> stops = resolveStops(req.stops());

        course.update(req.title(), req.description(), prefecture, req.imagePath(), req.isPublic());
//      Hibernate 는 flush 할 때 INSERT 를 DELETE 보다 먼저 낸다. 옛 정류장이 남은 채로 같은
//      (course_id, day_no, seq) 를 넣으면 UNIQUE 위반이므로, 옛 정류장 삭제를 먼저 flush 한다.
        course.clearStops();
        courseRepository.flush();
        stops.forEach(s -> course.addStop(s.toEntity(course)));
        return CourseResponse.from(course);
    }

//  멱등 아님 — 두 번째는 404 (review 관례). 정류장은 cascade 로 함께 지워진다.
    @Transactional
    public void delete(Long userId, Long id) {
        Course course = findVisible(id, userId);
        checkOwner(course, userId);
        courseRepository.delete(course);
    }

//  ---- 판정 ----

//  DB 의 NOT NULL · CHECK 에만 맡기면 위반이 500 으로 나간다. 먼저 400 으로 거른다.
//  빈 stops · 일차 건너뜀 · 같은 장소 두 번은 허용한다 (D-047).
    private void validate(CourseRequest req) {
        if (req.title() == null || req.title().isBlank()
                || req.prefecture() == null || req.prefecture().isBlank()
                || req.isPublic() == null
                || req.stops() == null) {
            throw new ApiException(ErrorCode.INVALID_COURSE);
        }
        for (CourseStopRequest s : req.stops()) {
            if (s == null || s.dayNo() == null || s.dayNo() < 1 || s.type() == null || s.targetId() == null) {
                throw new ApiException(ErrorCode.INVALID_COURSE);
            }
        }
    }

//  없는 코스와 남의 비공개 코스를 구분하지 않는다
    private Course findVisible(Long id, Long userId) {
        return courseRepository.findWithStopsById(id)
                .filter(c -> c.isVisibleTo(userId))
                .orElseThrow(() -> new ApiException(ErrorCode.COURSE_NOT_FOUND));
    }

//  기본 제공 코스(owner 없음)도 여기서 403
    private void checkOwner(Course course, Long userId) {
        if (!course.isOwnedBy(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

    private Prefecture findPrefecture(String name) {
        return prefectureRepository.findByName(name)
                .orElseThrow(() -> new ApiException(ErrorCode.PREFECTURE_NOT_FOUND));
    }

//  토큰은 유효한데 사용자가 지워졌으면 401 — AuthService.me 와 같은 규칙.
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
    }

//  ---- 정류장 ----

//  대상 존재를 확인하고 seq 를 매긴다. 같은 dayNo 안에서 배열에 나온 순서대로 1 부터 (D-046).
//  결과는 (dayNo, seq) 순으로 정렬해 돌려준다 — 새로 만든 정류장은 @OrderBy 를 거치지 않으므로
//  이 순서가 곧 작성 · 수정 응답의 순서다.
    private List<ResolvedStop> resolveStops(List<CourseStopRequest> requests) {
        Map<Integer, Integer> nextSeq = new HashMap<>();
        List<ResolvedStop> resolved = new ArrayList<>();
        for (CourseStopRequest r : requests) {
            int seq = nextSeq.merge(r.dayNo(), 1, Integer::sum);
            if (r.type() == StopType.DESTINATION) {
                Destination d = destinationRepository.findById(r.targetId())
                        .orElseThrow(() -> new ApiException(ErrorCode.DESTINATION_NOT_FOUND));
                resolved.add(new ResolvedStop(r.dayNo(), seq, d, null, r.memo()));
            } else {
                Festival f = festivalRepository.findById(r.targetId())
                        .orElseThrow(() -> new ApiException(ErrorCode.FESTIVAL_NOT_FOUND));
                resolved.add(new ResolvedStop(r.dayNo(), seq, null, f, r.memo()));
            }
        }
        resolved.sort(Comparator.comparing(ResolvedStop::dayNo).thenComparing(ResolvedStop::seq));
        return resolved;
    }

//  대상 확인을 코스 변경보다 먼저 끝내기 위한 중간 값. 둘 중 하나만 채워진다.
    private record ResolvedStop(Integer dayNo, Integer seq, Destination destination, Festival festival, String memo) {
        CourseStop toEntity(Course course) {
            return destination != null
                    ? CourseStop.of(course, dayNo, seq, destination, memo)
                    : CourseStop.of(course, dayNo, seq, festival, memo);
        }
    }

    private List<CourseSummaryResponse> toSummaries(List<Course> found) {
        return found.stream().map(CourseSummaryResponse::from).toList();
    }
}
