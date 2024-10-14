package com.marceldev.ourcompanylunchauth.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marceldev.ourcompanylunchauth.controller.UserController;
import com.marceldev.ourcompanylunchauth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false) // To disable security filters
public abstract class ControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @MockBean
  protected UserService userService;
}
