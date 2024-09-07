package com.marceldev.ourcompanylunchauth.controller;

import com.marceldev.ourcompanylunchauth.dto.SignInRequestDto;
import com.marceldev.ourcompanylunchauth.dto.SignUpRequestDto;
import com.marceldev.ourcompanylunchauth.dto.TokenResponseDto;
import com.marceldev.ourcompanylunchauth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "1 User", description = "회원 관련")
public class UserController {

  private final UserService userService;

  @Operation(
      summary = "회원가입",
      description = "회원가입시 이메일, 이름, 비밀번호 입력<br>"
          + "이메일을 아이디로 사용<br>"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "400", description = "errorCode: 1001 - 이미 존재하는 회원", content = @Content)
  })
  @PostMapping("/users/signup")
  public ResponseEntity<Void> signUp(
      @Validated @RequestBody SignUpRequestDto dto
  ) {
    userService.signUp(dto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "로그인",
      description = "아이디(이메일)과 비밀번호를 통해 로그인"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "OK")
  })
  @PostMapping("/users/signin")
  public ResponseEntity<TokenResponseDto> signIn(
      @Validated @RequestBody SignInRequestDto dto
  ) {
    TokenResponseDto token = userService.signIn(dto);
    return ResponseEntity.ok(token);
  }
}
