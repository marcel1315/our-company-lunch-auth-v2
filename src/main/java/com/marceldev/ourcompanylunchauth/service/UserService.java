package com.marceldev.ourcompanylunchauth.service;

import com.marceldev.ourcompanylunchauth.component.BusinessServerClient;
import com.marceldev.ourcompanylunchauth.component.EmailSender;
import com.marceldev.ourcompanylunchauth.dto.SendVerificationCodeRequest;
import com.marceldev.ourcompanylunchauth.dto.SignInRequest;
import com.marceldev.ourcompanylunchauth.dto.SignUpRequest;
import com.marceldev.ourcompanylunchauth.dto.TokenResponse;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private static final int VERIFICATION_CODE_VALID_SECOND = 60 * 3;

  private static final int VERIFICATION_CODE_LENGTH = 6;

  private final UserRepository userRepository;

  private final VerificationRepository verificationRepository;

  private final TokenProvider tokenProvider;

  private final EmailSender emailSender;

  private final PasswordEncoder passwordEncoder;

  private final BusinessServerClient businessServerClient;

  /**
   * Sign up to auth server. Also save the user profile(name and etc) to business server.
   */
  @Transactional
  public void signUp(SignUpRequest request) {
    checkAlreadyExistsUser(request.getEmail());

    Role role = Role.VIEWER;

    Verification verification = verificationRepository.findByEmail(request.getEmail())
        .orElseThrow(VerificationCodeNotFoundException::new);

    matchVerificationCode(request.getCode(), verification, request.getNow());

    String encPassword = passwordEncoder.encode(request.getPassword());
    User user = User.builder()
        .email(request.getEmail())
        .password(encPassword)
        .role(role)
        .build();

    userRepository.save(user);
    businessServerClient.signUp(request, role.toString());
    verificationRepository.delete(verification);
  }

  /**
   * ONLY FOR LOCAL, DEV environment. This is sign up, but without checking verification code. This
   * is for bulk user creation.
   */
  @Transactional
  public void mockSignUp(SignUpRequest request) {
    checkAlreadyExistsUser(request.getEmail());

    Role role = Role.VIEWER;

    String encPassword = passwordEncoder.encode(request.getPassword());
    User user = User.builder()
        .email(request.getEmail())
        .password(encPassword)
        .role(role)
        .build();

    userRepository.save(user);
    businessServerClient.signUp(request, role.toString());
  }

  /**
   * Sign in. Response is JWT token.
   */
  public TokenResponse signIn(SignInRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(UserNotExistException::new);

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new IncorrectPasswordException();
    }

    Role role = user.getRole();
    String token = tokenProvider.generateToken(user.getEmail(), role.toString());
    return new TokenResponse(token);
  }

  /**
   * Send verification code to the email.
   */
  @Transactional
  public void sendVerificationCode(SendVerificationCodeRequest request) {
    String email = request.getEmail();
    String code = GenerateVerificationCodeUtil.generate(VERIFICATION_CODE_LENGTH);

    sendVerificationCodeEmail(email, code);
    saveVerificationCodeToDb(email, code);
  }

  /**
   * Verification code remains when a user doesn't confirm the requested code and leave.
   */
  @Scheduled(cron = "${scheduler.clear-verification-code.cron}")
  public void clearUnusedVerificationCodes() {
    int rows = verificationRepository.deleteAllExpiredVerificationCode(LocalDateTime.now());
    log.info("Verification code clear: {} rows deleted", rows);
  }

  private void sendVerificationCodeEmail(String email, String code) {
    String subject = "[Our Company Lunch] Welcome!";
    String body = String.format("Verification code is %s. Enter this in signup field.", code);
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
}
