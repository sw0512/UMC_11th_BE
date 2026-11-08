package com.umc.movie.controller;

import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/nickname/{nickname}")
    public NicknameCheckResponse checkNickname(
            @PathVariable String nickname
    ) {
        return memberService.checkNickname(nickname);
    }

    @GetMapping("/email/{email}")
    public EmailCheckResponse checkEmail(
            @PathVariable String email
    ) {
        return memberService.checkEmail(email);
    }

 @PostMapping public org.springframework.http.ResponseEntity<?> signUp(@jakarta.validation.Valid @RequestBody com.umc.movie.dto.SignUpRequest r){return org.springframework.http.ResponseEntity.status(201).body(com.umc.movie.dto.ApiResponse.created("회원가입에 성공했습니다.",memberService.signUp(r)));}

}
