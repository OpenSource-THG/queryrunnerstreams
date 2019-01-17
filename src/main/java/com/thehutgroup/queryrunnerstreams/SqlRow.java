package com.thehutgroup.queryrunnerstreams;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class SqlRow {

  private ResultSet rs;

  SqlRow(ResultSet rs) {
    this.rs = rs;
  }

  public ResultSet getResultSet() {
    return rs;
  }

  public <T> T get(String columnLabel, Class<T> clazz) {
    try {
      switch (clazz.getName()) {
        case "java.time.Instant":
          return (T) rs.getTimestamp(columnLabel).toInstant();

        default:
          return (T) rs.getObject(columnLabel);
      }
    } catch (SQLException ex) {
      throw new RuntimeSQLException(ex);
    }
  }

  public String getString(String columnLabel) {
    return get(columnLabel, String.class);
  }

  public int getInt(String columnLabel) {
    Integer value = get(columnLabel, Integer.class);
    return value == null ? 0 : value;
  }

  public Instant getInstant(String columnLabel) {
    return get(columnLabel, Instant.class);
  }
}
