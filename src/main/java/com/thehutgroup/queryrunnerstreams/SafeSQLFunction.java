package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface SafeSQLFunction<T, R> {

  R apply(T input) throws SQLException;

  default Function<T, R> toFunction() {
    return input -> {
      try {
        return apply(input);
      } catch (SQLException ex) {
        throw new RuntimeSQLException(ex);
      }
    };
  }

  default Predicate<T> toPredicate() {
    return input -> Boolean.TRUE.equals(toFunction().apply(input));
  }
}
