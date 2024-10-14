package com.marceldev.ourcompanylunchauth.basic;

import com.marceldev.ourcompanylunchauth.component.BusinessServerClient;
import com.marceldev.ourcompanylunchauth.component.EmailSender;
import com.marceldev.ourcompanylunchauth.repository.UserRepository;
import com.marceldev.ourcompanylunchauth.repository.VerificationRepository;
import com.marceldev.ourcompanylunchauth.service.UserService;
import com.marceldev.ourcompanylunchcommon.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@Transactional
// @WithCustomUser(username = "jack@example.com")
public abstract class IntegrationTest {

  // --- Repository ---

  @Autowired
  protected UserRepository userRepository;

  @Autowired
  protected VerificationRepository verificationRepository;

  // --- Service ---

  @Autowired
  protected UserService userService;

  // --- Etc ---

  @Autowired
  protected PasswordEncoder passwordEncoder;

  @Autowired
  protected TokenProvider tokenProvider;

  @Autowired
  protected RestTemplate restTemplate;

  // --- Mock ---

  @MockBean
  protected EmailSender emailSender;

  @MockBean
  protected BusinessServerClient businessServerClient;
}
