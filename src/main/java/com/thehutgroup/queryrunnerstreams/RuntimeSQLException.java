package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;

public class RuntimeSQLException extends RuntimeException {
  private SQLException parent;

  public RuntimeSQLException(SQLException ex) {
    super(ex.getMessage(), ex);
    parent = ex;
  }

  public RuntimeSQLException(String message) {
    this(new SQLException(message));
  }

  public RuntimeSQLException(String message, Throwable cause) {
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

  public static SQLException extract(RuntimeSQLException ex) {
    return ex.getParent();
  }

  public static SQLException extract(SQLException ex) {
    return ex;
  }
}