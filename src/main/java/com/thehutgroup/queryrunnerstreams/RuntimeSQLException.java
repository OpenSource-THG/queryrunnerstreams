package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;

public class RuntimeSQLException extends RuntimeException {
  private final SQLException parent;

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

  @Override
  public String getMessage() {
    return parent.getMessage();
  }

  @Override
  public String getLocalizedMessage() {
    return parent.getLocalizedMessage();
  }

  @Override
  public Throwable getCause() {
    return parent.getCause();
  }

  @Override
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