package com.marceldev.ourcompanylunchauth.service;

import com.marceldev.ourcompanylunchauth.component.EmailSender;
import com.marceldev.ourcompanylunchauth.dto.BusinessServerSignUpRequestDto;
import com.marceldev.ourcompanylunchauth.dto.SendVerificationCodeDto;
import com.marceldev.ourcompanylunchauth.dto.SignInRequestDto;
import com.marceldev.ourcompanylunchauth.dto.SignUpRequestDto;
import com.marceldev.ourcompanylunchauth.dto.TokenResponseDto;
import com.marceldev.ourcompanylunchauth.entity.User;
import com.marceldev.ourcompanylunchauth.entity.Verification;
import com.marceldev.ourcompanylunchauth.exception.AlreadyExistUserException;
import com.marceldev.ourcompanylunchauth.exception.IncorrectPasswordException;
import com.marceldev.ourcompanylunchauth.exception.UserNotExistException;
import com.marceldev.ourcompanylunchauth.exception.VerificationCodeNotFoundException;
import com.marceldev.ourcompanylunchauth.model.Role;
import com.marceldev.ourcompanylunchauth.repository.UserRepository;
import com.marceldev.ourcompanylunchauth.repository.VerificationRepository;
import com.marceldev.ourcompanylunchauth.util.GenerateVerificationCodeUtil;
import com.marceldev.ourcompanylunchcommon.TokenProvider;
import java.time.LocalDateTime;
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

  @Value("${business-server-signup-path}")
  private String businessServerSignUpPath;

  private static final int VERIFICATION_CODE_VALID_SECOND = 60 * 3;

  private static final int VERIFICATION_CODE_LENGTH = 6;

  private final UserRepository userRepository;

  private final VerificationRepository verificationRepository;

  private final TokenProvider tokenProvider;

  private final EmailSender emailSender;

  private final PasswordEncoder passwordEncoder;

  private final RestTemplate restTemplate;

  /**
   * Sign up to auth server. Also save the user profile(name and etc) to business server.
   */
  @Transactional
  public void signUp(SignUpRequestDto dto) {
    checkAlreadyExistsUser(dto.getEmail());

    Role role = Role.VIEWER;

    Verification verification = verificationRepository.findByEmail(dto.getEmail())
        .orElseThrow(VerificationCodeNotFoundException::new);

    matchVerificationCode(dto.getCode(), verification, dto.getNow());

    String encPassword = passwordEncoder.encode(dto.getPassword());
    User user = User.builder()
        .email(dto.getEmail())
        .password(encPassword)
        .role(role)
        .build();

    userRepository.save(user);
    businessServerSignUp(dto, role.toString());
    verificationRepository.delete(verification);
  }

  /**
   * Sign in. Response is JWT token.
   */
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

  /**
   * Send verification code to the email.
   */
  @Transactional
  public void sendVerificationCode(SendVerificationCodeDto dto) {
    String email = dto.getEmail();
    String code = GenerateVerificationCodeUtil.generate(VERIFICATION_CODE_LENGTH);

    sendVerificationCodeEmail(email, code);
    saveVerificationCodeToDb(email, code);
  }

  private void sendVerificationCodeEmail(String email, String code) {
    String subject = "[Our Company Lunch] Welcome!";
    String body = String.format("Verification Code is %s. Enter this in signup field.", code);
    emailSender.sendMail(email, subject, body);
  }

  private void saveVerificationCodeToDb(String email, String code) {
    // Remove if already exists.
    verificationRepository.findByEmail(email).ifPresent(verificationRepository::delete);

    // Save new verification code.
    Verification verification = Verification.builder()
        .code(code)
        .expirationAt(LocalDateTime.now().plusSeconds(VERIFICATION_CODE_VALID_SECOND))
        .email(email)
        .build();

    verificationRepository.save(verification);
  }

  private void matchVerificationCode(String code, Verification verification, LocalDateTime now) {
    if (now.isAfter(verification.getExpirationAt())) {
      throw new VerificationCodeNotFoundException();
    }

    if (!verification.getCode().equals(code)) {
      throw new VerificationCodeNotFoundException();
    }
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
