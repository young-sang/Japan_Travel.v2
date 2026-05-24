package com.japantravel.controller;

import com.japantravel.dto.Dtos.Festival;
import com.japantravel.repository.FestivalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/festivals")
public class FestivalController {

    private final FestivalRepository repo;
    public FestivalController(FestivalRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Festival> list(@RequestParam(required = false) String prefecture,
                               @RequestParam(required = false) Integer month) {
        return repo.findAll(prefecture, month);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Festival> one(@PathVariable long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
