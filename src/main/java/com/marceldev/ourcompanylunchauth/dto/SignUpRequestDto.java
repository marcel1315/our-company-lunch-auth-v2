package com.marceldev.ourcompanylunchauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpRequestDto {

  @NotNull
  @Email
  @Schema(example = "hello@company.com")
  private String email;

  @NotNull
  @Schema(description = "Password should be 8~50 long and include a English letter and a number", example = "secretpw12")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,50}$")
  private String password;
}