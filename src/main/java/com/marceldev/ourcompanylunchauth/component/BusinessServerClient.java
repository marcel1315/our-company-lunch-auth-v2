package com.marceldev.ourcompanylunchauth.component;

import com.marceldev.ourcompanylunchauth.dto.BusinessServerSignUpRequest;
import com.marceldev.ourcompanylunchauth.dto.SignUpRequest;
import com.marceldev.ourcompanylunchcommon.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessServerClient {

  @Value("${business-server-url}")
  private String businessServerUrl;

  @Value("${business-server-signup-path}")
  private String businessServerSignUpPath;

  private final TokenProvider tokenProvider;

  private final RestTemplate restTemplate;

  public void signUp(SignUpRequest dto, String role) {
    BusinessServerSignUpRequest request = BusinessServerSignUpRequest.builder()
        .name(dto.getName())
        .build();
    String token = tokenProvider.generateToken(dto.getEmail(), role);
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);

    try {
      restTemplate.exchange(
          businessServerUrl + businessServerSignUpPath,
          HttpMethod.POST,
          new HttpEntity<>(request, headers),
          Void.class
      );
    } catch (HttpClientErrorException e) {
      log.error(e.toString());
      throw e;
    }
  }
}
