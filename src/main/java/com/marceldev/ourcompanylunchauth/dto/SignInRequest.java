package com.marceldev.ourcompanylunchauth.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignInRequest {

  @NotBlank
  @Email
  @Schema(description = "Company email", example = "hello@company.com")
  private String email;

  @NotBlank
  @Schema(example = "secretpw12")
  private String password;

  @Builder
  public SignInRequest(String email, String password) {
    this.email = email;
    this.password = password;
  }
}
