package com.marceldev.ourcompanylunchauth.exception;

public class AlreadyExistUserException extends CustomException {

  public AlreadyExistUserException() {
    super("User email already exist.");
  }
}
