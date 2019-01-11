package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;

public class RuntimeSQLException extends RuntimeException {
  private SQLException parent;

  RuntimeSQLException(SQLException ex) {
    super(ex.getMessage(), ex);
    parent = ex;
  }

  public SQLException getParent() {
    return parent;
  }
}