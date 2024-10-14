package com.marceldev.ourcompanylunchauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SendVerificationCodeRequest {

  @NotNull
  @Email
  @Schema(example = "hello@company.com")
  private final String email;

  @Builder
  private SendVerificationCodeRequest(String email) {
    this.email = email;
  }
}
