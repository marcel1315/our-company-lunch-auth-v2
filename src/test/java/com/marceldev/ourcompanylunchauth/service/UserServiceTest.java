package com.marceldev.ourcompanylunchauth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.marceldev.ourcompanylunchauth.basic.IntegrationTest;
import com.marceldev.ourcompanylunchauth.dto.SendVerificationCodeRequest;
import com.marceldev.ourcompanylunchauth.dto.SignInRequest;
import com.marceldev.ourcompanylunchauth.dto.SignUpRequest;
import com.marceldev.ourcompanylunchauth.dto.TokenResponse;
import com.marceldev.ourcompanylunchauth.entity.User;
import com.marceldev.ourcompanylunchauth.entity.Verification;
import com.marceldev.ourcompanylunchauth.exception.AlreadyExistUserException;
import com.marceldev.ourcompanylunchauth.exception.IncorrectPasswordException;
import com.marceldev.ourcompanylunchauth.exception.UserNotExistException;
import com.marceldev.ourcompanylunchauth.model.Role;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UserServiceTest extends IntegrationTest {

  @Test
  @DisplayName("Sign Up - Success")
  void sign_up() {
    // given
    SignUpRequest request = createSignUpRequest("hello@example.com", "123456", "abc123123");
    Verification verification = createVerification("hello@example.com", "123456");
    verificationRepository.save(verification);

    // when
    userService.signUp(request);

    // then
    User user = userRepository.findByEmail("hello@example.com")
        .orElseThrow();
    assertThat(user).extracting(User::getEmail, User::getRole)
        .contains("hello@example.com", Role.VIEWER);
  }

  @Test
  @DisplayName("Sign Up - Fail(Email already exist)")
  void sign_up_fail_exist_user() {
    // given
    signUpUser("hello@example.com", "abc123123");
    SignUpRequest request = createSignUpRequest("hello@example.com", "123456", "abab1212");

    // when // then
    assertThrows(AlreadyExistUserException.class,
        () -> userService.signUp(request));
  }

  @Test
  @DisplayName("Sign In - Success")
  void sign_in() {
    // given
    signUpUser("hello@example.com", "abc123123");
    SignInRequest request = createSignInRequest("hello@example.com", "abc123123");

    // when
    TokenResponse tokenResponse = userService.signIn(request);

    // then
    assertThat(tokenProvider.validateToken(tokenResponse.getToken())).isTrue();
  }

  @Test
  @DisplayName("Sign In - Fail(No email found)")
  void sign_in_fail_no_email() {
    // given
    SignInRequest request = createSignInRequest("hello@example.com", "abc123123");

    // when // then
    assertThrows(UserNotExistException.class,
        () -> userService.signIn(request));
  }

  @Test
  @DisplayName("Sign In - Fail(Incorrect password)")
  void sign_in_fail_incorrect_password() {
    // given
    signUpUser("hello@example.com", "abc123123");
    SignInRequest request = createSignInRequest("hello@example.com", "abab1212");

    // when // then
    assertThrows(IncorrectPasswordException.class,
        () -> userService.signIn(request));
  }

  @Test
  @DisplayName("Send Verification Code - Success")
  void send_verification_code() {
    // given
    SendVerificationCodeRequest request = SendVerificationCodeRequest.builder()
        .email("hello@example.com")
        .build();

    // when
    userService.sendVerificationCode(request);

    // then
    ArgumentCaptor<String> captorEmail = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> captorSubject = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> captorBody = ArgumentCaptor.forClass(String.class);

    verify(emailSender).sendMail(captorEmail.capture(), captorSubject.capture(),
        captorBody.capture());
    assertThat(captorEmail.getValue()).isEqualTo("hello@example.com");
    assertThat(captorSubject.getValue().contains("Our Company Lunch")).isTrue();
    assertThat(captorBody.getValue().contains("Verification code")).isTrue();
  }

  private void signUpUser(String email, String password) {
    SignUpRequest request = createSignUpRequest(email, "123123", password);
    Verification verification = createVerification(email, "123123");
    verificationRepository.save(verification);
    userService.signUp(request);
  }

  private SignUpRequest createSignUpRequest(String email, String code, String password) {
    return SignUpRequest.builder()
        .email(email)
        .name("John")
        .password(password)
        .code(code)
        .build();
  }

  private SignInRequest createSignInRequest(String email, String password) {
    return SignInRequest.builder()
        .email(email)
        .password(password)
        .build();
  }

  private static Verification createVerification(String email, String code) {
    return Verification.builder()
        .email(email)
        .expirationAt(LocalDateTime.now().plusMinutes(3))
        .code(code)
        .build();
  }
}