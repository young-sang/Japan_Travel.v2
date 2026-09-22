package com.japantravel.destination.service;

import com.japantravel.common.error.NotFoundException;
import com.japantravel.destination.dto.DestinationResponse;
import com.japantravel.destination.entity.Destination;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.prefecture.repository.PrefectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final PrefectureRepository prefectureRepository;

//  DTO 로 바꿔서 내보낸다. open-in-view 가 꺼져 있어 컨트롤러에서 LAZY 인
//  prefecture 를 건드리면 LazyInitializationException 이 난다.
    public List<DestinationResponse> findAll(String prefecture) {
        if (prefecture == null || prefecture.isBlank()) {
            return toResponses(destinationRepository.findAllByOrderByIdDesc());
        }
//      없는 현 이름은 404. 실재하는 현인데 0건이면 빈 목록으로 내려간다.
        if (!prefectureRepository.existsByName(prefecture)) {
            throw new NotFoundException("그런 현이 없습니다: " + prefecture);
        }
        return toResponses(destinationRepository.findByPrefectureNameOrderByIdDesc(prefecture));
    }

    public DestinationResponse findById(Long id) {
        return destinationRepository.findById(id)
                .map(DestinationResponse::from)
                .orElseThrow(() -> new NotFoundException("여행지를 찾을 수 없습니다: " + id));
    }

    private List<DestinationResponse> toResponses(List<Destination> found) {
        return found.stream().map(DestinationResponse::from).toList();
    }
}
