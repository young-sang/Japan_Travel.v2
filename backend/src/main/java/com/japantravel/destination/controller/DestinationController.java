package com.japantravel.destination.controller;

import com.japantravel.common.web.ApiResponse;
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
    public ApiResponse<List<DestinationResponse>> list(@RequestParam(required = false) String prefecture) {
        return ApiResponse.ok(destinationService.findAll(prefecture));
    }

    @GetMapping("/{id}")
    public ApiResponse<DestinationResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(destinationService.findById(id));
    }
}
