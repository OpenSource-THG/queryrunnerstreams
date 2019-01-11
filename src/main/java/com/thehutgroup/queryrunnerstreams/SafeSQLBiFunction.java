package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;
import java.util.function.BiFunction;

@FunctionalInterface
public interface SafeSQLBiFunction<T, U, R> {

  R apply(T inputT, U inputU) throws SQLException;

  default BiFunction<T, U, R> toBiFunction() {
    return (inputT, inputU) -> {
      try {
        return apply(inputT, inputU);
      } catch (SQLException ex) {
        throw new RuntimeSQLException(ex);
      }
    };
  }
}
