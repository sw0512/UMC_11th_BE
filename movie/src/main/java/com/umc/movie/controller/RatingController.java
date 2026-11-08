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

 @PostMapping("/{memberId}/ratings") public org.springframework.http.ResponseEntity<?> create(@PathVariable Long memberId,@jakarta.validation.Valid @RequestBody com.umc.movie.dto.CreateRatingRequest r){return org.springframework.http.ResponseEntity.status(201).body(com.umc.movie.dto.ApiResponse.created("평점 등록에 성공했습니다.",ratingService.create(memberId,r)));}
 @PatchMapping("/{memberId}/ratings/{ratingId}") public Object update(@PathVariable Long memberId,@PathVariable Long ratingId,@jakarta.validation.Valid @RequestBody com.umc.movie.dto.UpdateRatingRequest r){ratingService.update(memberId,ratingId,r);return com.umc.movie.dto.ApiResponse.ok("평점 수정에 성공했습니다.",null);}
 @DeleteMapping("/{memberId}/ratings/{ratingId}") public org.springframework.http.ResponseEntity<Void> delete(@PathVariable Long memberId,@PathVariable Long ratingId){ratingService.delete(memberId,ratingId);return org.springframework.http.ResponseEntity.noContent().build();}

}
