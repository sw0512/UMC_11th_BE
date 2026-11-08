package com.umc.movie.dto;
import jakarta.validation.constraints.*;
public record SignUpRequest(@NotBlank @Email @Size(max=254) String email,
 @NotBlank @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$") @Size(max=72) String password,
 @NotBlank @Size(min=2,max=12) String nickname) {}