package com.umc.movie.exception;
import com.umc.movie.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice public class GlobalExceptionHandler {
 @ExceptionHandler(ApiException.class) public ResponseEntity<?> business(ApiException e){return error(e.status,e.code,e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validation(MethodArgumentNotValidException e){return error(400,"COMMON400",e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).collect(java.util.stream.Collectors.joining(", ")));}
 @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> malformed(Exception e){return error(400,"COMMON400","요청 형식이 올바르지 않습니다.");}
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<?> status(ResponseStatusException e){return error(e.getStatusCode().value(),"COMMON"+e.getStatusCode().value(),e.getReason());}
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<?> integrity(DataIntegrityViolationException e){String detail = String.valueOf(e.getMostSpecificCause().getMessage()).toLowerCase(java.util.Locale.ROOT);
 if (detail.contains("uk_member_email")) return error(409,"MEMBER4091","이미 사용 중인 이메일입니다.");
 if (detail.contains("uk_member_nickname")) return error(409,"MEMBER4092","이미 사용 중인 닉네임입니다.");
 return error(409,"DB409","데이터 제약 조건에 위배되는 요청입니다.");}
 private ResponseEntity<?> error(int status,String code,String message){return ResponseEntity.status(status).body(new ApiResponse<>(false,code,message,null));}
}