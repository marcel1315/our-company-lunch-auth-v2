package com.marceldev.ourcompanylunchauth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SendVerificationCodeRequest {

  @NotBlank
  @Email
  @Schema(example = "hello@company.com")
  private final String email;

  @JsonCreator
  @Builder
  private SendVerificationCodeRequest(String email) {
    this.email = email;
  }
}
