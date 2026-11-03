package com.japantravel.course;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.course.dto.CourseRequest;
import com.japantravel.course.dto.CourseResponse;
import com.japantravel.course.dto.CourseStopRequest;
import com.japantravel.course.dto.CourseStopResponse;
import com.japantravel.course.dto.StopType;
import com.japantravel.course.entity.Course;
import com.japantravel.course.repository.CourseRepository;
import com.japantravel.course.service.CourseService;
import com.japantravel.destination.entity.Destination;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.festival.entity.Festival;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.prefecture.entity.Prefecture;
import com.japantravel.prefecture.repository.PrefectureRepository;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.japantravel.support.Fixtures.destination;
import static com.japantravel.support.Fixtures.festival;
import static com.japantravel.support.Fixtures.prefecture;
import static com.japantravel.support.Fixtures.user;
import static com.japantravel.support.Fixtures.withId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

//  판정 순서 (D-047): 본문 값 → 코스 존재 → 작성자 → 현 · 정류장 대상
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock CourseRepository courseRepository;
    @Mock PrefectureRepository prefectureRepository;
    @Mock UserRepository userRepository;
    @Mock DestinationRepository destinationRepository;
    @Mock FestivalRepository festivalRepository;

    @InjectMocks CourseService courseService;

    private final Prefecture tokyo = prefecture("도쿄도");

    private static void assertCode(Runnable call, ErrorCode code) {
        assertThatThrownBy(call::run).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(code);
    }

    private static CourseRequest request(String title, Boolean isPublic, List<CourseStopRequest> stops) {
        return new CourseRequest(title, "설명", "도쿄도", null, isPublic, stops);
    }

    private Course course(long id, User owner, boolean isPublic) {
        return withId(new Course("코스", null, tokyo, null, owner, isPublic), id);
    }

    // ---- 본문 값 ----

    @Test
    void 필수_값이_빠지면_아무것도_읽기_전에_INVALID_COURSE() {
        List<CourseRequest> bad = new ArrayList<>(List.of(
                request(null, true, List.of()),
                request(" ", true, List.of()),
                request("t", null, List.of()),
                request("t", true, null),
                new CourseRequest("t", null, null, null, true, List.of()),
                request("t", true, List.of(new CourseStopRequest(0, StopType.DESTINATION, 1L, null))),
                request("t", true, List.of(new CourseStopRequest(1, null, 1L, null))),
                request("t", true, List.of(new CourseStopRequest(1, StopType.FESTIVAL, null, null)))));
        List<CourseStopRequest> nullStop = new ArrayList<>();
        nullStop.add(null);
        bad.add(request("t", true, nullStop));

        for (CourseRequest req : bad) {
            assertCode(() -> courseService.create(1L, req), ErrorCode.INVALID_COURSE);
            assertCode(() -> courseService.update(1L, 99L, req), ErrorCode.INVALID_COURSE);
        }
        verifyNoInteractions(courseRepository, prefectureRepository, destinationRepository, festivalRepository);
    }

    // ---- 작성 ----

    @Test
    void 정류장은_일차_안에서_배열_순서대로_seq_를_매기고_일차순으로_정렬한다() {
        when(prefectureRepository.findByName("도쿄도")).thenReturn(Optional.of(tokyo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        Destination d10 = destination(10L), d11 = destination(11L);
        Festival f20 = festival(20L);
        when(destinationRepository.findById(10L)).thenReturn(Optional.of(d10));
        when(destinationRepository.findById(11L)).thenReturn(Optional.of(d11));
        when(festivalRepository.findById(20L)).thenReturn(Optional.of(f20));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse res = courseService.create(1L, request("t", true, List.of(
                new CourseStopRequest(2, StopType.DESTINATION, 11L, "둘째 날"),
                new CourseStopRequest(1, StopType.DESTINATION, 10L, null),
                new CourseStopRequest(1, StopType.FESTIVAL, 20L, null),
                new CourseStopRequest(2, StopType.DESTINATION, 10L, "같은 장소 두 번"))));

        assertThat(res.stops())
                .extracting(CourseStopResponse::dayNo, CourseStopResponse::seq, CourseStopResponse::type, CourseStopResponse::targetId)
                .containsExactly(
                        tuple(1, 1, StopType.DESTINATION, 10L),
                        tuple(1, 2, StopType.FESTIVAL, 20L),
                        tuple(2, 1, StopType.DESTINATION, 11L),
                        tuple(2, 2, StopType.DESTINATION, 10L));
        assertThat(res.ownerId()).isEqualTo(1L);
    }

    @Test
    void 정류장_대상이_없으면_저장하지_않고_404() {
        when(prefectureRepository.findByName("도쿄도")).thenReturn(Optional.of(tokyo));
        when(festivalRepository.findById(20L)).thenReturn(Optional.empty());

        assertCode(() -> courseService.create(1L, request("t", true,
                List.of(new CourseStopRequest(1, StopType.FESTIVAL, 20L, null)))), ErrorCode.FESTIVAL_NOT_FOUND);
        verify(courseRepository, never()).save(any());
    }

    @Test
    void 없는_현이면_404() {
        when(prefectureRepository.findByName("도쿄도")).thenReturn(Optional.empty());

        assertCode(() -> courseService.create(1L, request("t", true, List.of())), ErrorCode.PREFECTURE_NOT_FOUND);
    }

    // ---- 보이는 것 · 작성자 ----

    @Test
    void 비공개_코스는_주인에게만_보이고_남과_비로그인에게는_COURSE_NOT_FOUND() {
        when(courseRepository.findWithStopsById(5L)).thenReturn(Optional.of(course(5L, user(1L), false)));

        assertThat(courseService.findById(5L, 1L).id()).isEqualTo(5L);
        assertCode(() -> courseService.findById(5L, 2L), ErrorCode.COURSE_NOT_FOUND);
        assertCode(() -> courseService.findById(5L, null), ErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    void 남의_비공개_코스는_수정해도_403_이_아니라_404() {
        when(courseRepository.findWithStopsById(5L)).thenReturn(Optional.of(course(5L, user(1L), false)));

        assertCode(() -> courseService.update(2L, 5L, request("t", true, List.of())), ErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    void 남의_공개_코스와_기본_제공_코스는_FORBIDDEN() {
        when(courseRepository.findWithStopsById(5L)).thenReturn(Optional.of(course(5L, user(1L), true)));
        when(courseRepository.findWithStopsById(6L)).thenReturn(Optional.of(course(6L, null, true)));

        assertCode(() -> courseService.update(2L, 5L, request("t", true, List.of())), ErrorCode.FORBIDDEN);
        assertCode(() -> courseService.delete(2L, 5L), ErrorCode.FORBIDDEN);
        assertCode(() -> courseService.update(1L, 6L, request("t", true, List.of())), ErrorCode.FORBIDDEN);
        verify(courseRepository, never()).delete(any());
    }

    @Test
    void 수정은_정류장을_비우고_다시_채운다() {
        Course course = course(5L, user(1L), true);
        when(courseRepository.findWithStopsById(5L)).thenReturn(Optional.of(course));
        when(prefectureRepository.findByName("도쿄도")).thenReturn(Optional.of(tokyo));
        Destination d10 = destination(10L);
        when(destinationRepository.findById(10L)).thenReturn(Optional.of(d10));

        courseService.update(1L, 5L, request("t", true, List.of(new CourseStopRequest(1, StopType.DESTINATION, 10L, null))));
        CourseResponse res = courseService.update(1L, 5L,
                request("바꿈", false, List.of(new CourseStopRequest(3, StopType.DESTINATION, 10L, null))));

        assertThat(res.title()).isEqualTo("바꿈");
        assertThat(res.isPublic()).isFalse();
        assertThat(res.updatedAt()).isNotNull();
        assertThat(res.stops()).extracting(CourseStopResponse::dayNo, CourseStopResponse::seq).containsExactly(tuple(3, 1));
    }

    @Test
    void 공개_목록은_없는_현이면_404() {
        when(prefectureRepository.existsByName("없는현")).thenReturn(false);

        assertCode(() -> courseService.findPublic("없는현"), ErrorCode.PREFECTURE_NOT_FOUND);
    }
}
