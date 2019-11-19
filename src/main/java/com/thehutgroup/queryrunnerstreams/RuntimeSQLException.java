package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;

public class RuntimeSQLException extends RuntimeException {
  private SQLException parent;

  public RuntimeSQLException(final SQLException ex) {
    super(ex.getMessage(), ex);
    parent = ex;
  }

  public RuntimeSQLException(final String message) {
    this(new SQLException(message));
  }

  public RuntimeSQLException(final String message, final Throwable cause) {
    this(new SQLException(message, cause));
  }

  public SQLException getParent() {
    return parent;
  }

  public String getMessage() {
    return parent.getMessage();
  }

  public String getLocalizedMessage() {
    return parent.getLocalizedMessage();
  }

  public Throwable getCause() {
    return parent.getCause();
  }

  public String toString() {
    return parent.toString();
  }

  public static SQLException extract(final RuntimeSQLException ex) {
    return ex.getParent();
  }

  public static SQLException extract(final SQLException ex) {
    return ex;
  }
}