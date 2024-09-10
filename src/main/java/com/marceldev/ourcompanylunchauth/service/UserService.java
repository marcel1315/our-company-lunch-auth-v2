package com.marceldev.ourcompanylunchauth.service;

import com.marceldev.ourcompanylunchauth.dto.BusinessServerSignUpRequestDto;
import com.marceldev.ourcompanylunchauth.dto.SignInRequestDto;
import com.marceldev.ourcompanylunchauth.dto.SignUpRequestDto;
import com.marceldev.ourcompanylunchauth.dto.TokenResponseDto;
import com.marceldev.ourcompanylunchauth.entity.User;
import com.marceldev.ourcompanylunchauth.exception.AlreadyExistUserException;
import com.marceldev.ourcompanylunchauth.exception.IncorrectPasswordException;
import com.marceldev.ourcompanylunchauth.exception.UserNotExistException;
import com.marceldev.ourcompanylunchauth.model.Role;
import com.marceldev.ourcompanylunchauth.repository.UserRepository;
import com.marceldev.ourcompanylunchauth.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  @Value("${business-server-url}")
  private String businessServerUrl;

  private static final String businessServerSignUpPath = "/members/signup";

  private final UserRepository userRepository;
  private final TokenProvider tokenProvider;
  private final PasswordEncoder passwordEncoder;
  private final RestTemplate restTemplate;

  @Transactional
  public void signUp(SignUpRequestDto dto) {
    checkAlreadyExistsUser(dto.getEmail());
    Role role = Role.VIEWER;

    String encPassword = passwordEncoder.encode(dto.getPassword());
    User user = User.builder()
        .email(dto.getEmail())
        .password(encPassword)
        .role(role)
        .build();

    userRepository.save(user);
    businessServerSignUp(dto, role.toString());
  }

  public TokenResponseDto signIn(SignInRequestDto dto) {
    User user = userRepository.findByEmail(dto.getEmail())
        .orElseThrow(UserNotExistException::new);

    if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
      throw new IncorrectPasswordException();
    }

    Role role = user.getRole();
    String token = tokenProvider.generateToken(user.getEmail(), role.toString());
    return new TokenResponseDto(token);
  }

  private void checkAlreadyExistsUser(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new AlreadyExistUserException();
    }
  }

  private void businessServerSignUp(SignUpRequestDto dto, String role) {
    BusinessServerSignUpRequestDto request = BusinessServerSignUpRequestDto.builder()
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
