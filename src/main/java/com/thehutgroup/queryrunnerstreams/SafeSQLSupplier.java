package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@FunctionalInterface
public interface SafeSQLSupplier<T> {

  T get() throws SQLException;

  default Supplier<T> toSupplier() {
    return () -> {
      try {
        return get();
      } catch (SQLException ex) {
        throw new RuntimeSQLException(ex);
      }
    };
  }
}
