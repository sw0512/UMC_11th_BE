package com.umc.movie.controller;

import com.umc.movie.dto.RatingResponse;
import com.umc.movie.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @GetMapping("/{memberId}/ratings")
    public List<RatingResponse> getRatings(
            @PathVariable Long memberId
    ) {
        return ratingService.getRatings(memberId);
    }
}