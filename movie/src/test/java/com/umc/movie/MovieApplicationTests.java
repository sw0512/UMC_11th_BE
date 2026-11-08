package com.umc.movie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@org.springframework.test.context.ActiveProfiles("evidence")
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:tests;MODE=MySQL", "spring.jpa.hibernate.ddl-auto=create-drop"})
class MovieApplicationTests {

	@Test
	void contextLoads() {
	}


 @org.springframework.beans.factory.annotation.Autowired com.umc.movie.service.MemberService members;
 @org.springframework.beans.factory.annotation.Autowired com.umc.movie.repository.MemberRepository repository;
 @org.springframework.beans.factory.annotation.Autowired org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder;
 @Test void storesBcryptAndSeparatesDuplicateCodes() {
  var request = new com.umc.movie.dto.SignUpRequest("verify@example.com", "Umc!1234", "verify");
  var response = members.signUp(request);
  var stored = repository.findById(response.memberId()).orElseThrow();
  org.junit.jupiter.api.Assertions.assertNotEquals(request.password(), stored.getPassword());
  org.junit.jupiter.api.Assertions.assertEquals(60, stored.getPassword().length());
  org.junit.jupiter.api.Assertions.assertTrue(encoder.matches(request.password(), stored.getPassword()));
  var email = org.junit.jupiter.api.Assertions.assertThrows(com.umc.movie.exception.ApiException.class, () -> members.signUp(request));
  org.junit.jupiter.api.Assertions.assertEquals("MEMBER4091", email.code);
  var nickname = org.junit.jupiter.api.Assertions.assertThrows(com.umc.movie.exception.ApiException.class, () -> members.signUp(new com.umc.movie.dto.SignUpRequest("other@example.com", "Umc!1234", "verify")));
  org.junit.jupiter.api.Assertions.assertEquals("MEMBER4092", nickname.code);
 }
}
