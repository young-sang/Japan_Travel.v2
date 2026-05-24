package com.japantravel.controller;

import com.japantravel.dto.Dtos.Destination;
import com.japantravel.repository.DestinationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationRepository repo;
    public DestinationController(DestinationRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Destination> list(@RequestParam(required = false) String prefecture,
                                  @RequestParam(required = false) String tag) {
        return repo.findAll(prefecture, tag);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Destination> one(@PathVariable long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
