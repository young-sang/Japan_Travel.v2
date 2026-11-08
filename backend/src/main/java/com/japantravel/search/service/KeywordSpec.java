package com.japantravel.search.service;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;

//  통합 검색 조건 (D-083). 낱말마다 "칸 중 하나라도 포함" 을 만들고, 낱말끼리는 모두 맞아야 한다.
//  낱말 수가 검색어마다 달라서 고정된 JPQL 대신 Specification 으로 조립한다
public final class KeywordSpec {

    private KeywordSpec() {
    }

//  fields 는 "name", "prefecture.name" 처럼 점으로 이어 쓴다
    public static <T> Specification<T> allWords(List<String> words, String... fields) {
        return (root, query, cb) -> cb.and(words.stream()
                .map(w -> cb.or(Arrays.stream(fields)
                        .map(f -> cb.like(path(root, f), "%" + w + "%"))
                        .toArray(Predicate[]::new)))
                .toArray(Predicate[]::new));
    }

    private static <T> Path<String> path(Root<T> root, String field) {
        Path<?> p = root;
        for (String part : field.split("\\.")) {
            p = p.get(part);
        }
        @SuppressWarnings("unchecked")
        Path<String> s = (Path<String>) p;
        return s;
    }
}
