package com.marceldev.ourcompanylunchauth.controller;

import com.marceldev.ourcompanylunchauth.dto.SignUpRequestDto;
import com.marceldev.ourcompanylunchauth.exception.AlreadyExistUserException;
import com.marceldev.ourcompanylunchauth.exception.ErrorResponse;
import com.marceldev.ourcompanylunchauth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Profile({"local", "mac"}) // In other profiles, this endpoint will not open. "mac" is dev environment.
@RestController
@RequiredArgsConstructor
@Tag(name = "1 User")
public class UserControllerDev {

  private final UserService userService;

  @Operation(
      summary = "Mock Sign Up (ONLY for local, dev profile).",
      description = "Can sign up with random code, then sign in with it.<br>"
          + "Require email and password.<br>"
          + "Email will be username."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "400", description = "errorCode: 1001 - Already exist user", content = @Content)
  })
  @PostMapping("/users/mocksignup")
  public ResponseEntity<Void> mockSignUp(
      @Validated @RequestBody SignUpRequestDto dto
  ) {
    userService.mockSignUp(dto);
    return ResponseEntity.ok().build();
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handle(AlreadyExistUserException e) {
    return ErrorResponse.badRequest(1001, e.getMessage());
  }
}
