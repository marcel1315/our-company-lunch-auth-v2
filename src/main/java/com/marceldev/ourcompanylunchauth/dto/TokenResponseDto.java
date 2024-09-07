package com.marceldev.ourcompanylunchauth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenResponseDto {

  private final String token;
}
