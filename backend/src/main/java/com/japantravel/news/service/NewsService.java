package com.japantravel.news.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.news.dto.NewsRequest;
import com.japantravel.news.dto.NewsResponse;
import com.japantravel.news.dto.NewsSummaryResponse;
import com.japantravel.news.entity.News;
import com.japantravel.news.repository.NewsRepository;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//  ADMIN 확인은 SecurityConfig 가 경로로 끝냈다. 여기서는 존재와 본문만 판정한다 (D-079)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private static final int TITLE_MAX = 200;

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    public List<NewsSummaryResponse> findAll() {
        return newsRepository.findAllByOrderByCreatedAtDescIdDesc().stream().map(NewsSummaryResponse::from).toList();
    }

    public NewsResponse findById(Long id) {
        return NewsResponse.from(get(id));
    }

//  검증 → 작성자 조회 → 저장
    @Transactional
    public NewsResponse create(Long userId, NewsRequest req) {
        validate(req);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
        News news = newsRepository.save(
                new News(author, req.title().strip(), req.body(), blankToNull(req.imagePath()), blankToNull(req.imageCredit())));
        return NewsResponse.from(news);
    }

//  조회(404) → 검증(400) → 변경
    @Transactional
    public NewsResponse update(Long id, NewsRequest req) {
        News news = get(id);
        validate(req);
        news.update(req.title().strip(), req.body(), blankToNull(req.imagePath()), blankToNull(req.imageCredit()));
        return NewsResponse.from(news);
    }

    @Transactional
    public void delete(Long id) {
        newsRepository.delete(get(id));
    }

    private News get(Long id) {
        return newsRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NEWS_NOT_FOUND));
    }

    private void validate(NewsRequest req) {
        if (req.title() == null || req.title().isBlank() || req.title().strip().length() > TITLE_MAX
                || req.body() == null || req.body().isBlank()) {
            throw new ApiException(ErrorCode.INVALID_NEWS);
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }
}
