package com.thehutgroup.queryrunnerstreams;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;
import org.apache.commons.dbutils.ResultSetHandler;

public class QueryRunnerStreamer {

  public static ResultSetHandler<BasicSQLStream<ResultSet>> stream() throws SQLException {
    try {
      return rs -> new BasicSQLStream<>(StreamSupport.stream(new ResultSetSpliterator(rs), false));
    } catch (RuntimeSQLException ex) {
      throw ex.getParent();
    }
  }

  public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
      SafeSQLFunction<? super T, ? extends U> keyExtractor) {
    return Comparator.comparing(keyExtractor.toFunction());
  }

  private static long getSizeOfResultSet(ResultSet rs) {
    try {
      rs.last();
      int size = rs.getRow();
      rs.first();
      return size;
    } catch (SQLException ex) {
      throw new RuntimeSQLException(ex);
    }
  }

  private static class ResultSetSpliterator extends Spliterators.AbstractSpliterator<ResultSet> {
    private final ResultSet rs;

    private ResultSetSpliterator(ResultSet rs) {
      super(getSizeOfResultSet(rs), Spliterator.ORDERED);
      this.rs = rs;
    }

    @Override
    public boolean tryAdvance(Consumer<? super ResultSet> action) {
      try {
        if (rs.next()) {
          action.accept(rs);
          return true;
        } else {
          return false;
        }
      } catch (SQLException ex) {
        throw new RuntimeSQLException(ex);
      }
    }
  }
}