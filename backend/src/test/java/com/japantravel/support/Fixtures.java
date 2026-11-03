package com.japantravel.support;

import com.japantravel.destination.entity.Destination;
import com.japantravel.festival.entity.Festival;
import com.japantravel.prefecture.entity.Prefecture;
import com.japantravel.user.entity.User;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

//  서비스 단위 테스트의 엔티티 (D-062). DB 를 거치지 않으므로 id 를 직접 넣는다.
//  여행지 · 축제 · 현은 생성자가 없어(시드로만 들어온다) 가짜로 만든다. 쓰지 않는 스텁에 걸리지 않게 LENIENT.
//  가짜를 만들며 스텁을 걸기 때문에 when(...) 의 인자 안에서 부르면 UnfinishedStubbingException — 변수에 먼저 담는다.
public final class Fixtures {

    private Fixtures() {
    }

    public static User user(long id) {
        User user = new User("user" + id, "hash-" + id, "닉" + id);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static <T> T withId(T entity, long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    public static Prefecture prefecture(String name) {
        Prefecture prefecture = mock(Prefecture.class, withSettings().strictness(Strictness.LENIENT));
        when(prefecture.getName()).thenReturn(name);
        return prefecture;
    }

    public static Destination destination(long id) {
        Destination destination = mock(Destination.class, withSettings().strictness(Strictness.LENIENT));
        Prefecture prefecture = prefecture("도쿄도");
        when(destination.getId()).thenReturn(id);
        when(destination.getName()).thenReturn("여행지" + id);
        when(destination.getPrefecture()).thenReturn(prefecture);
//      Mockito 기본값은 Double 에도 0.0 이다. 실제 엔티티처럼 리뷰가 없으면 null
        when(destination.getAvgRating()).thenReturn(null);
        return destination;
    }

    public static Festival festival(long id) {
        Festival festival = mock(Festival.class, withSettings().strictness(Strictness.LENIENT));
        Prefecture prefecture = prefecture("교토부");
        when(festival.getId()).thenReturn(id);
        when(festival.getName()).thenReturn("축제" + id);
        when(festival.getPrefecture()).thenReturn(prefecture);
        return festival;
    }
}
