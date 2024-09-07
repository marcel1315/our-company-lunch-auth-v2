package com.marceldev.ourcompanylunchauth.exception;

public class UserNotExistException extends CustomException {

  public UserNotExistException() {
    super("User not exist.");
  }
}
