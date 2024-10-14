package com.marceldev.ourcompanylunchauth.exception.handler;

import com.marceldev.ourcompanylunchauth.exception.CustomException;
import com.marceldev.ourcompanylunchauth.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handler(
      CustomException e,
      HttpServletRequest request
  ) {
    log.error("CustomException, {}, {}, {}", request.getRequestURI(), e.getMessage(),
        String.valueOf(e.getCause()));

    return ErrorResponse.serverError(9000, e.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException e,
      HttpServletRequest request
  ) {
    log.error("MethodArgumentNotValidException, {}, {}, {}", request.getRequestURI(),
        e.getMessage(), String.valueOf(e.getCause()));

    StringBuilder sb = new StringBuilder();
    e.getBindingResult().getFieldErrors().forEach(error -> {
      sb.append(String.format("%s: %s\n", error.getField(), error.getDefaultMessage()));
    });
    return ErrorResponse.badRequest(8000, sb.toString());
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handlerAll(
      Exception e,
      HttpServletRequest request
  ) {
    log.error("Exception, {}, {}, {}", request.getRequestURI(), e.getMessage(),
        String.valueOf(e.getCause()));

    return ErrorResponse.serverError(9000, "unknown");
  }
}