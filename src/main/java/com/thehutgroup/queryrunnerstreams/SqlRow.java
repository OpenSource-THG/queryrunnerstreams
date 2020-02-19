package com.thehutgroup.queryrunnerstreams;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

public class SqlRow {

  private ResultSet rs;

  SqlRow(final ResultSet rs) {
    this.rs = rs;
  }

  public ResultSet getResultSet() {
    return rs;
  }

  public <T> T get(final String columnLabel, final Class<T> clazz) {
    return get(
        () -> rs.getObject(columnLabel),
        intermediateClazz -> rs.getObject(columnLabel, intermediateClazz),
        clazz);
  }

  //Note: these indices start at 1
  public <T> T get(final int columnIndex, final Class<T> clazz) {
    return get(
        () -> rs.getObject(columnIndex),
        intermediateClazz -> rs.getObject(columnIndex, intermediateClazz),
        clazz);
  }

  @SuppressFBWarnings("URV_UNRELATED_RETURN_VALUES")
  private <T> T get(
      final SafeSQLSupplier<Object> supplier,
      final SafeSQLFunction<Class<?>, Object> intermediateSupplier,
      final Class<T> clazz) {
    try {
      switch (clazz.getName()) {
        case "java.time.Instant":
          Timestamp timestamp = (Timestamp) intermediateSupplier.apply(Timestamp.class);
          return timestamp == null ? null : (T) timestamp.toInstant();

        case "java.time.LocalDateTime":
          try {
            //Some SQL Drivers (namely SQL Server 7.1.0+) can get a LocalDateTime from
            // the database directly, so lets try and do that
            return (T) intermediateSupplier.apply(clazz);
          } catch (RuntimeSQLException ex) {
            //Failing that, get a Timestamp and convert it so LocalDateTime using Javas clock
            Timestamp localTimestamp = (Timestamp) intermediateSupplier.apply(Timestamp.class);
            return localTimestamp == null ? null : (T) localTimestamp.toLocalDateTime();
          }

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

  public Instant getInstantFromLong(final String columnLabel) {
    Long timestamp = get(columnLabel, Long.class);
    return timestamp == null ? null : Instant.ofEpochMilli(timestamp);
  }

  public LocalDateTime getLocalDateTime(final String columnLabel) {
    return get(columnLabel, LocalDateTime.class);
  }
}
