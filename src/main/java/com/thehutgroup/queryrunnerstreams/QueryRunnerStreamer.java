package com.thehutgroup.queryrunnerstreams;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.dbutils.ResultSetHandler;

public class QueryRunnerStreamer {

  @SuppressFBWarnings("LEST_LOST_EXCEPTION_STACK_TRACE")
  public static ResultSetHandler<Stream<SqlRow>> stream() throws SQLException {
    try {
      return rs -> StreamSupport.stream(new ResultSetSpliterator(rs), false);
    } catch (RuntimeSQLException ex) {
      throw ex.getParent();
    }
  }

  public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
      SafeSQLFunction<? super T, ? extends U> keyExtractor) {
    return Comparator.comparing(keyExtractor.toFunction());
  }

  private static class ResultSetSpliterator extends Spliterators.AbstractSpliterator<SqlRow> {
    private final ResultSet rs;

    private ResultSetSpliterator(ResultSet rs) {
      super(Long.MAX_VALUE, Spliterator.ORDERED);
      this.rs = rs;
    }

    @Override
    public boolean tryAdvance(Consumer<? super SqlRow> action) {
      try {
        if (rs.next()) {
          action.accept(new SqlRow(rs));
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
