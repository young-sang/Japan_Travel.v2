package com.japantravel.festival.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.festival.dto.FestivalResponse;
import com.japantravel.festival.entity.Festival;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.prefecture.repository.PrefectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final PrefectureRepository prefectureRepository;

    public List<FestivalResponse> findAll(String prefecture, Integer month) {
        validateMonth(month);

        boolean noPrefecture = (prefecture == null || prefecture.isBlank());
//      없는 현 이름은 404. 실재하는 현인데 0건이면 빈 목록으로 내려간다.
        if (!noPrefecture && !prefectureRepository.existsByName(prefecture)) {
            throw new ApiException(ErrorCode.PREFECTURE_NOT_FOUND);
        }

        List<Festival> found;
        if (noPrefecture && month == null) {
            found = festivalRepository.findAllByOrderByMonthAscIdDesc();
        } else if (noPrefecture) {
            found = festivalRepository.findByMonthOrderByMonthAscIdDesc(month);
        } else if (month == null) {
            found = festivalRepository.findByPrefectureNameOrderByMonthAscIdDesc(prefecture);
        } else {
            found = festivalRepository
                    .findByPrefectureNameAndMonthOrderByMonthAscIdDesc(prefecture, month);
        }
        return found.stream().map(FestivalResponse::from).toList();
    }

    public FestivalResponse findById(Long id) {
        return festivalRepository.findById(id)
                .map(FestivalResponse::from)
                .orElseThrow(() -> new ApiException(ErrorCode.FESTIVAL_NOT_FOUND));
    }

//  13월은 애초에 존재할 수 없는 잘못된 요청이므로 404 가 아니라 400 이다.
    private void validateMonth(Integer month) {
        if (month != null && (month < 1 || month > 12)) {
            throw new ApiException(ErrorCode.INVALID_MONTH);
        }
    }
}
