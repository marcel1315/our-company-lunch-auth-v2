package com.marceldev.ourcompanylunchauth.service;

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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final TokenProvider tokenProvider;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void signUp(SignUpRequestDto dto) {
    checkAlreadyExistsUser(dto.getEmail());

    String encPassword = passwordEncoder.encode(dto.getPassword());
    User member = User.builder()
        .email(dto.getEmail())
        .password(encPassword)
        .role(Role.VIEWER)
        .build();

    userRepository.save(member);
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
}
