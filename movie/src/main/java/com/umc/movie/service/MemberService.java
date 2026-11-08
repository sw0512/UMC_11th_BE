package com.umc.movie.service;

import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public NicknameCheckResponse checkNickname(String nickname) {
        boolean exists = memberRepository.existsByNickname(nickname);

        return new NicknameCheckResponse(!exists);
    }

    public EmailCheckResponse checkEmail(String email) {
        boolean exists = memberRepository.existsByEmail(email);

        return new EmailCheckResponse(!exists);
    }

 private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;
 @Transactional public com.umc.movie.dto.MemberResponse signUp(com.umc.movie.dto.SignUpRequest r){
 if(memberRepository.existsByEmail(r.email())) throw new com.umc.movie.exception.ApiException(409,"MEMBER4091","이미 사용 중인 이메일입니다.");
 if(memberRepository.existsByNickname(r.nickname())) throw new com.umc.movie.exception.ApiException(409,"MEMBER4092","이미 사용 중인 닉네임입니다.");
 var m=memberRepository.saveAndFlush(new com.umc.movie.entity.Member(r.email(),passwordEncoder.encode(r.password()),r.nickname()));
 return new com.umc.movie.dto.MemberResponse(m.getMemberId(),m.getEmail(),m.getNickname());
 }

}
