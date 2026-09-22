package com.japantravel.destination.controller;

import com.japantravel.destination.dto.DestinationResponse;
import com.japantravel.destination.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @GetMapping
    public List<DestinationResponse> list(@RequestParam(required = false) String prefecture) {
        return destinationService.findAll(prefecture);
    }

    @GetMapping("/{id}")
    public DestinationResponse detail(@PathVariable Long id) {
        return destinationService.findById(id);
    }
}
