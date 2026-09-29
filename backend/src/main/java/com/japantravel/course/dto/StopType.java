package com.japantravel.course.dto;

//  정류장이 가리키는 대상의 종류. 요청과 응답이 같이 쓴다 (D-046).
//  그 밖의 값은 역직렬화에서 실패해 400 MALFORMED_REQUEST 가 된다.
public enum StopType {
    DESTINATION,
    FESTIVAL
}
