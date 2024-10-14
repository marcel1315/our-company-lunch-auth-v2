package com.marceldev.ourcompanylunchauth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.marceldev.ourcompanylunchauth.basic.ControllerTest;
import com.marceldev.ourcompanylunchauth.dto.SendVerificationCodeRequest;
import com.marceldev.ourcompanylunchauth.dto.SignInRequest;
import com.marceldev.ourcompanylunchauth.dto.SignUpRequest;
import com.marceldev.ourcompanylunchauth.dto.TokenResponse;
import com.marceldev.ourcompanylunchauth.exception.AlreadyExistUserException;
import com.marceldev.ourcompanylunchauth.exception.IncorrectPasswordException;
import com.marceldev.ourcompanylunchauth.exception.SignInFailException;
import com.marceldev.ourcompanylunchauth.exception.VerificationCodeNotFoundException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;

class UserControllerTest extends ControllerTest {

  // --- Sign up ---

  @Test
  @DisplayName("Sign up - Success")
  void signup() throws Exception {
    // given
    SignUpRequest request = SignUpRequest.builder()
        .email("hello@example.com")
        .password("abc123123")
        .name("John")
        .code("111222")
        .build();

    // when // then
    mockMvc.perform(
            post("/users/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Sign up - Fail(email must not be blank)")
  void signup_fail_required1() throws Exception {
    // given
    SignUpRequest request = SignUpRequest.builder()
        .email(" ")
        .password("abc123123")
        .name("John")
        .code("111222")
        .build();

    // when // then
    mockMvc.perform(
            post("/users/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("8000"));
  }

  @ParameterizedTest
  @DisplayName("Sign up - Fail(password constraint)")
  @CsvSource({
      " ,400", // bad: empty
      "abcdabcd,400", // bad: only letter
      "12341234,400", // bad: only number
      "!@#$%^&*(),400", // bad: only special character
      "abc123123,200", // good: letter + number
      "abc12!@!@,200", // good: letter + number + special character
      "abcd!@!@,400", // bad: letter + special character
      "1234!@!@,400", // bad: number + special character
      "5678abcd,200", // good: number + letter
      "abc1234,400", // bad: short (must be 8 ~ 30)
      "abcd1234,200", // good: 8 character long
      "abcd_1234_1234_1234_1234_1234_!,400", // bad: 31 character long (must be 8 ~ 30)
      "abcd_1234_1234_1234_1234_1234_,200", // good: 30 character long
  })
  void signup_fail_required2(String password, int statusCode) throws Exception {
    // given
    SignUpRequest request = SignUpRequest.builder()
        .email("hello@example.com")
        .password(password)
        .name("John")
        .code("111222")
        .build();

    // when // then
    mockMvc.perform(
            post("/users/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().is(statusCode));
  }

  @Test
  @DisplayName("Sign up - Fail(name must not be blank)")
  void signup_fail_required3() throws Exception {
    // given
    SignUpRequest request = SignUpRequest.builder()
        .email("hello@example.com")
        .password("abcd1234")
        .name("")
        .code("111222")
        .build();

    // when // then
    mockMvc.perform(
            post("/users/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("8000"));
  }

  @Test
  @DisplayName("Sign up - Fail(code must not be blank)")
  void signup_fail_required4() throws Exception {
    // given
    SignUpRequest request = SignUpRequest.builder()
        .email("hello@example.com")
        .password("abcd1234")
        .name("John")
        .code("")
        .build();

    // when // then
    mockMvc.perform(
            post("/users/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("8000"));
  }

  @Test
  @DisplayName("Sign up - Fail(verification code doesn't match)")
  void signup_fail_verification_code() throws Exception {
    // given
    SignUpRequest request = SignUpRequest.builder()
        .email("hello@example.com")
        .password("abc123123")
        .name("John")
        .code("111222")
        .build();

    doThrow(VerificationCodeNotFoundException.class)
        .when(userService)
        .signUp(any(SignUpRequest.class));

    // when // then
    mockMvc.perform(
            post("/users/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("1002"));
  }

  @Test
  @DisplayName("Sign up - Fail(email already exist)")
  void signup_fail_email_exist() throws Exception {
    // given
    SignUpRequest request = SignUpRequest.builder()
        .email("hello@example.com")
        .password("abc123123")
        .name("John")
        .code("111222")
        .build();

    doThrow(AlreadyExistUserException.class)
        .when(userService)
        .signUp(any(SignUpRequest.class));

    // when // then
    mockMvc.perform(
            post("/users/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("1001"));
  }

  // --- Sign in ---

  @Test
  @DisplayName("Sign in - Success")
  void signin() throws Exception {
    // given
    SignInRequest request = SignInRequest.builder()
        .email("hello@example.com")
        .password("abc123123")
        .build();

    given(userService.signIn(any(SignInRequest.class)))
        .willReturn(new TokenResponse("token"));

    // when // then
    mockMvc.perform(
            post("/users/signin")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("token"));
  }

  @Test
  @DisplayName("Sign in - Fail(email must not be blank)")
  void signin_fail_required1() throws Exception {
    // given
    SignInRequest request = SignInRequest.builder()
        .email(" ")
        .password("abc123123")
        .build();

    // when // then
    mockMvc.perform(
            post("/users/signin")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("8000"));
  }

  @Test
  @DisplayName("Sign in - Fail(password must not be blank)")
  void signin_fail_required2() throws Exception {
    // given
    SignInRequest request = SignInRequest.builder()
        .email("hello@example.com")
        .password(" ")
        .build();

    // when // then
    mockMvc.perform(
            post("/users/signin")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("8000"));
  }

  @Test
  @DisplayName("Sign in - Fail(Incorrect password)")
  void signin_fail_incorrect_password() throws Exception {
    // given
    SignInRequest request = SignInRequest.builder()
        .email("hello@example.com")
        .password("abc111222")
        .build();

    given(userService.signIn(any(SignInRequest.class)))
        .willThrow(IncorrectPasswordException.class);

    // when // then
    mockMvc.perform(
            post("/users/signin")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("1003"));
  }

  @Test
  @DisplayName("Sign in - Fail(Fail in general)")
  void signin_fail_general() throws Exception {
    // given
    SignInRequest request = SignInRequest.builder()
        .email("hello@example.com")
        .password("abc111222")
        .build();

    given(userService.signIn(any(SignInRequest.class)))
        .willThrow(SignInFailException.class);

    // when // then
    mockMvc.perform(
            post("/users/signin")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("1004"));
  }

  @Test
  @DisplayName("Send Verification Code - Success")
  void send_verification_code() throws Exception {
    // given
    SendVerificationCodeRequest request = SendVerificationCodeRequest.builder()
        .email("hello@example.com")
        .build();

    // when // then
    mockMvc.perform(
            post("/users/send-verification-code")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Send Verification Code - Fail(email must no be blank")
  void send_verification_code_fail_required() throws Exception {
    // given
    SendVerificationCodeRequest request = SendVerificationCodeRequest.builder()
        .email(" ")
        .build();

    // when // then
    mockMvc.perform(
            post("/users/send-verification-code")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
        )
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("8000"));
  }
}