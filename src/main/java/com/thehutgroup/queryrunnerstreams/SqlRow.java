package com.thehutgroup.queryrunnerstreams;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class SqlRow {

  private ResultSet rs;

  SqlRow(final ResultSet rs) {
    this.rs = rs;
  }

  public ResultSet getResultSet() {
    return rs;
  }

  public <T> T get(final String columnLabel, final Class<T> clazz) {
    return get(() -> rs.getObject(columnLabel), clazz);
  }

  //Note: these indices start at 1
  public <T> T get(final int columnIndex, final Class<T> clazz) {
    return get(() -> rs.getObject(columnIndex), clazz);
  }

  //Note: these indices start at 1
  @SuppressFBWarnings("URV_UNRELATED_RETURN_VALUES")
  private <T> T get(final SafeSQLSupplier<Object> supplier, final Class<T> clazz) {
    try {
      switch (clazz.getName()) {
        case "java.time.Instant":
          Timestamp timestamp = (Timestamp) supplier.get();
          return timestamp == null ? null : (T) timestamp.toInstant();

        default:
          return (T) supplier.get();
      }
    } catch (SQLException ex) {
      throw new RuntimeSQLException(ex);
    }
  }

  public String getString(final String columnLabel) {
    return get(columnLabel, String.class);
  }

  public int getInt(final String columnLabel) {
    Integer value = get(columnLabel, Integer.class);
    return value == null ? 0 : value;
  }

  public long getLong(final String columnLabel) {
    Long value = get(columnLabel, Long.class);
    return value == null ? 0 : value;
  }

  public boolean getBoolean(final String columnLabel) {
    return Boolean.TRUE.equals(get(columnLabel, Boolean.class));
  }

  public Instant getInstant(final String columnLabel) {
    return get(columnLabel, Instant.class);
  }

  public Instant getInstantFromBigInt(final String columnLabel) {
    Long timestamp = get(columnLabel, Long.class);
    return timestamp == null ? null : Instant.ofEpochMilli(timestamp);
  }
}
