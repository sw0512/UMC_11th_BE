package com.umc.movie.service;

import com.umc.movie.dto.RatingResponse;
import com.umc.movie.entity.Rating;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingService {

    private final MemberRepository memberRepository;
    private final RatingRepository ratingRepository;

    public List<RatingResponse> getRatings(Long memberId) {

        if (!memberRepository.existsById(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "회원을 찾을 수 없습니다."
            );
        }

        List<Rating> ratings =
                ratingRepository.findByMember_MemberIdOrderByRatingIdDesc(memberId);

        return ratings.stream()
                .map(RatingResponse::from)
                .toList();
    }

 @Transactional public Long create(Long memberId,com.umc.movie.dto.CreateRatingRequest r){
 var m=memberRepository.findById(memberId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"존재하지 않는 회원입니다."));
 if(ratingRepository.existsByMember_MemberIdAndMovieId(memberId,r.movieId())) throw new com.umc.movie.exception.ApiException(409,"RATING409","이미 이 영화에 평점을 남기셨습니다.");
 return ratingRepository.saveAndFlush(new Rating(m,r.movieId(),r.score(),r.comment())).getRatingId(); }
 private Rating owned(Long memberId,Long ratingId){var r=ratingRepository.findById(ratingId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"존재하지 않는 평점입니다.")); if(!r.getMember().getMemberId().equals(memberId))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"본인의 평점만 수정하거나 삭제할 수 있습니다.");return r;}
 @Transactional public void update(Long memberId,Long ratingId,com.umc.movie.dto.UpdateRatingRequest r){owned(memberId,ratingId).update(r.score(),r.comment());}
 @Transactional public void delete(Long memberId,Long ratingId){ratingRepository.delete(owned(memberId,ratingId));}

}
