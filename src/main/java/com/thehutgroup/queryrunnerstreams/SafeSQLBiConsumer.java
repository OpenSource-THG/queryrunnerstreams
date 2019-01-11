package com.thehutgroup.queryrunnerstreams;

import java.sql.SQLException;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface SafeSQLBiConsumer<T, U> {

  void accept(T inputT, U inputU) throws SQLException;

  default BiConsumer<T, U> toBiConsumer() {
    return (inputT, inputU) -> {
      try {
        accept(inputT, inputU);
      } catch (SQLException ex) {
        throw new RuntimeSQLException(ex);
      }
    };
  }
}
