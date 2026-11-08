# 영화 평점 API · 6주차

Java 21 / Spring Boot 3.5 / Spring Data JPA.

## 실행

```sh
./gradlew bootRun --args='--spring.profiles.active=evidence'
```

H2 파일 DB(MySQL 호환 모드), 포트 18086으로 실행합니다. 기본 MySQL 프로필은 DB_PASSWORD 환경 변수를 설정하고 umc_movie 스키마를 준비해야 합니다. 기본 ddl-auto=validate이므로 테이블은 미리 생성해야 합니다.

| 기능 | Method | 경로 | 성공 |
|---|---|---|---|
| 회원가입 | POST | /members | 201 |
| 평점 등록 | POST | /members/{memberId}/ratings | 201 |
| 평점 수정 | PATCH | /members/{memberId}/ratings/{ratingId} | 200 |
| 평점 삭제 | DELETE | /members/{memberId}/ratings/{ratingId} | 204 |

회원가입 요청: email, password, nickname. 평점 등록: movieId, score(1~5), comment(최대 500자). 수정: score, comment.

비밀번호는 bcrypt 해시로 저장하고 응답 DTO에서 제외합니다. memberId는 학습 단계의 경로 입력이며 실제 인증 기능은 포함하지 않습니다.

이메일/닉네임 충돌은 각각 MEMBER4091/MEMBER4092로 구분하며 DB UNIQUE 충돌도 전역 예외 처리합니다. 기존 스키마에는 uk_member_email/uk_member_nickname 제약 이름을 적용해야 같은 구분이 가능합니다.

H2 환경에서 필수 API 15건 및 이메일·닉네임 동시 요청(각 8건, 1 성공/7 충돌)을 검증했습니다. MySQL 환경은 별도 검증이 필요합니다.
