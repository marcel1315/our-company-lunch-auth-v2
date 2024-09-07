package com.marceldev.ourcompanylunchauth.exception;

public class SignInFailException extends CustomException {

  public SignInFailException(Throwable cause) {
    super("Sign in failed.", cause);
  }
}
