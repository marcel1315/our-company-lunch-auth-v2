package com.marceldev.ourcompanylunchauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignUpRequest {

  @NotBlank
  @Email
  @Schema(example = "hello@company.com")
  private final String email;

  @NotBlank
  @Schema(description = "Password should be 8~30 long and include a English letter and a number", example = "secretpw12")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d\\S]{8,30}$")
  private final String password;

  @NotBlank
  @Schema(example = "Marcel")
  private final String name;

  @NotBlank
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