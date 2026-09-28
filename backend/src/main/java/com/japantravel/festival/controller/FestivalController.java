package com.japantravel.festival.controller;

import com.japantravel.common.web.ApiResponse;
import com.japantravel.festival.dto.FestivalResponse;
import com.japantravel.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @GetMapping
    public ApiResponse<List<FestivalResponse>> list(@RequestParam(required = false) String prefecture,
                                       @RequestParam(required = false) Integer month) {
        return ApiResponse.ok(festivalService.findAll(prefecture, month));
    }

    @GetMapping("/{id}")
    public ApiResponse<FestivalResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(festivalService.findById(id));
    }
}
