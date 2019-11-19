package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;

public class EmptyResultDataAccessException extends SQLException {

  EmptyResultDataAccessException(final String message, final Throwable cause) {
    super(message, cause);
  }

  EmptyResultDataAccessException(final String message) {
    super(message);
  }

}
