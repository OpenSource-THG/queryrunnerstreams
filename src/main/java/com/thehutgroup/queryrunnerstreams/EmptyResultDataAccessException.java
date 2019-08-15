package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;

public class EmptyResultDataAccessException extends SQLException {

  EmptyResultDataAccessException(String message, Throwable cause) {
    super(message, cause);
  }

  EmptyResultDataAccessException(String message) {
    super(message);
  }

}
