package com.marceldev.ourcompanylunchauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessServerSignUpRequest {

  @NotNull
  @Schema(example = "Marcel")
  private String name;
}
