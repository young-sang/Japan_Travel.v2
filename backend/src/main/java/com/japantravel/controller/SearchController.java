package com.japantravel.controller;

import com.japantravel.repository.CourseRepository;
import com.japantravel.repository.DestinationRepository;
import com.japantravel.repository.FestivalRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final DestinationRepository destRepo;
    private final FestivalRepository festRepo;
    private final CourseRepository courseRepo;

    public SearchController(DestinationRepository d, FestivalRepository f, CourseRepository c) {
        this.destRepo = d; this.festRepo = f; this.courseRepo = c;
    }

    @GetMapping
    public Map<String, Object> search(@RequestParam String q) {
        if (q == null || q.isBlank()) return Map.of("destinations", List.of(), "festivals", List.of(), "courses", List.of());
        String needle = q.trim().toLowerCase();
        var dests = destRepo.findAll(null, null).stream()
                .filter(d -> contains(d.name(), needle) || contains(d.description(), needle) || contains(d.prefecture(), needle))
                .limit(20).toList();
        var fests = festRepo.findAll(null, null).stream()
                .filter(f -> contains(f.name(), needle) || contains(f.description(), needle) || contains(f.prefecture(), needle))
                .limit(20).toList();
        var courses = courseRepo.findAll(null, null, null, null).stream()
                .filter(c -> contains(c.title(), needle) || contains(c.prefecture(), needle))
                .limit(20).toList();
        return Map.of("destinations", dests, "festivals", fests, "courses", courses);
    }

    private boolean contains(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase().contains(needle);
    }
}
