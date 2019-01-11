package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface SafeSQLConsumer<T> {

  void accept(T input) throws SQLException;

  default Consumer<T> toConsumer() {
    return input -> {
      try {
        accept(input);
      } catch (SQLException ex) {
        throw new RuntimeSQLException(ex);
      }
    };
  }
}
