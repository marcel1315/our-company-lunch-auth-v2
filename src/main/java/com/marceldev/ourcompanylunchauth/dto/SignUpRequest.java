package com.marceldev.ourcompanylunchauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignUpRequest {

  @NotNull
  @Email
  @Schema(example = "hello@company.com")
  private final String email;

  @NotNull
  @Schema(description = "Password should be 8~50 long and include a English letter and a number", example = "secretpw12")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,50}$")
  private final String password;

  @NotNull
  @Schema(example = "Marcel")
  private final String name;

  @NotNull
  @Schema(example = "123456")
  private final String code;

  @JsonIgnore // Don't take this field from client.
  private final LocalDateTime now = LocalDateTime.now();

  @Builder
  private SignUpRequest(String email, String password, String name, String code) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.code = code;
  }
}