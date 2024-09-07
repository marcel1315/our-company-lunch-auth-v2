package com.marceldev.ourcompanylunchauth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private TokenProvider tokenProvider;

  @InjectMocks
  private UserService userService;

  User user1;

  void setupUser1() {
    user1 = User.builder()
        .id(10L)
        .email("hello@example.com")
        .password("password")
        .role(Role.VIEWER)
        .build();
  }

  @Test
  @DisplayName("Sign Up - Success")
  void sign_up() {
    //given
    SignUpRequestDto dto = SignUpRequestDto.builder()
        .email("hello@example.com")
        .password("abc123123")
        .build();

    //when
    userService.signUp(dto);
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

    //then
    verify(userRepository).save(captor.capture());
    assertEquals(captor.getValue().getEmail(), "hello@example.com");
    assertNotEquals(captor.getValue().getPassword(), "abc123123");
    verify(passwordEncoder).encode(eq("abc123123"));
  }

  @Test
  @DisplayName("Sign Up - Fail(Email already exist)")
  void sign_up_fail_exist_user() {
    //given
    SignUpRequestDto dto = SignUpRequestDto.builder()
        .email("hello@example.com")
        .password("abc123123")
        .build();

    //when
    when(userRepository.existsByEmail("hello@example.com"))
        .thenReturn(true);

    //then
    assertThrows(AlreadyExistUserException.class,
        () -> userService.signUp(dto));
  }

  @Test
  @DisplayName("Sign In - Success")
  void sign_in() {
    //given
    setupUser1();
    SignInRequestDto dto = SignInRequestDto.builder()
        .email("hello@example.com")
        .password("a1234")
        .build();

    //when
    when(userRepository.findByEmail("hello@example.com"))
        .thenReturn(Optional.of(user1));
    when(passwordEncoder.matches(eq(dto.getPassword()), any()))
        .thenReturn(true);
    when(tokenProvider.generateToken("hello@example.com", Role.VIEWER.toString()))
        .thenReturn("jwttoken");
    TokenResponseDto tokenResponseDto = userService.signIn(dto);

    //then
    assertEquals(tokenResponseDto.getToken(), "jwttoken");
  }

  @Test
  @DisplayName("Sign In - Fail(No email found)")
  void sign_in_fail_no_email() {
    //given
    SignInRequestDto dto = SignInRequestDto.builder()
        .email("hello@example.com")
        .password("a1234")
        .build();

    //when
    when(userRepository.findByEmail("hello@example.com"))
        .thenReturn(Optional.empty());

    //then
    assertThrows(UserNotExistException.class,
        () -> userService.signIn(dto));
  }

  @Test
  @DisplayName("Sign In - Fail(Incorrect password)")
  void sign_in_fail_incorrect_password() {
    //given
    setupUser1();
    SignInRequestDto dto = SignInRequestDto.builder()
        .email("hello@example.com")
        .password("a1234")
        .build();

    //when
    when(userRepository.findByEmail("hello@example.com"))
        .thenReturn(Optional.of(user1));
    when(passwordEncoder.matches(eq(dto.getPassword()), any()))
        .thenReturn(false);

    //then
    assertThrows(IncorrectPasswordException.class,
        () -> userService.signIn(dto));
  }
}