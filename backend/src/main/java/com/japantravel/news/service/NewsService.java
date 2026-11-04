package com.japantravel.news.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.news.dto.NewsResponse;
import com.japantravel.news.dto.NewsSummaryResponse;
import com.japantravel.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;

    public List<NewsSummaryResponse> findAll() {
        return newsRepository.findAllByOrderByCreatedAtDescIdDesc().stream().map(NewsSummaryResponse::from).toList();
    }

    public NewsResponse findById(Long id) {
        return newsRepository.findById(id)
                .map(NewsResponse::from)
                .orElseThrow(() -> new ApiException(ErrorCode.NEWS_NOT_FOUND));
    }
}
