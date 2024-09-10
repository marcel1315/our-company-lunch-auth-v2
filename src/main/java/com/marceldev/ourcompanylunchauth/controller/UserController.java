package com.marceldev.ourcompanylunchauth.controller;

import com.marceldev.ourcompanylunchauth.dto.SignInRequestDto;
import com.marceldev.ourcompanylunchauth.dto.SignUpRequestDto;
import com.marceldev.ourcompanylunchauth.dto.TokenResponseDto;
import com.marceldev.ourcompanylunchauth.exception.AlreadyExistUserException;
import com.marceldev.ourcompanylunchauth.exception.ErrorResponse;
import com.marceldev.ourcompanylunchauth.exception.IncorrectPasswordException;
import com.marceldev.ourcompanylunchauth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "1 User")
public class UserController {

  private final UserService userService;

  @Operation(
      summary = "Sign Up",
      description = "Require email and password<br>"
          + "Email will be username"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "400", description = "errorCode: 1001 - Already exist user", content = @Content)
  })
  @PostMapping("/users/signup")
  public ResponseEntity<Void> signUp(
      @Validated @RequestBody SignUpRequestDto dto
  ) {
    userService.signUp(dto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Sign In",
      description = "Require email and password"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "400", description = "errorCode: 1003 - Incorrect password", content = @Content)
  })
  @PostMapping("/users/signin")
  public ResponseEntity<TokenResponseDto> signIn(
      @Validated @RequestBody SignInRequestDto dto
  ) {
    TokenResponseDto token = userService.signIn(dto);
    return ResponseEntity.ok(token);
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handle(AlreadyExistUserException e) {
    return ErrorResponse.badRequest(1001, e.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handle(IncorrectPasswordException e) {
    return ErrorResponse.badRequest(1003, e.getMessage());
  }
}
