package com.marceldev.ourcompanylunchauth.exception;

public class VerificationCodeNotFoundException extends CustomException {

  public VerificationCodeNotFoundException() {
    super("Verification code doesn't exist.");
  }
}
