package com.marceldev.ourcompanylunchauth.exception;

public class IncorrectPasswordException extends CustomException {

  public IncorrectPasswordException() {
    super("Incorrect password.");
  }
}
