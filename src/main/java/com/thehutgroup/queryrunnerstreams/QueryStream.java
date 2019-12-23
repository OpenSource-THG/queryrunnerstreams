package com.thehutgroup.queryrunnerstreams;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class QueryStream {

  //Hide the implicit public constructor
  private QueryStream() {
  }

  public static Stream<SqlRow> of(final ResultSet rs) throws SQLException {
    return of(rs, null);
  }

  @SuppressFBWarnings("LEST_LOST_EXCEPTION_STACK_TRACE") //Intentionally throwing the parent
  public static Stream<SqlRow> of(final ResultSet rs, final Runnable onClose) throws SQLException {
    try {
      Stream<SqlRow> baseStream = StreamSupport.stream(new ResultSetSpliterator(rs), false);
      if (onClose != null) {
        baseStream = ClosableStreamInvocationHandler.wrap(baseStream, onClose);
      }
      return baseStream;
    } catch (RuntimeSQLException ex) {
      throw ex.getParent();
    }
  }

  public static <T> Stream<T> map(final ResultSet rs, final SafeSQLFunction<SqlRow, T> mapper)
      throws SQLException {
    return of(rs)
        .map(mapper.toFunction());
  }

  public static <T> List<T> toList(final ResultSet rs, final SafeSQLFunction<SqlRow, T> mapper)
      throws SQLException {
    return map(rs, mapper)
        .collect(Collectors.toList());
  }

  public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
      final SafeSQLFunction<? super T, ? extends U> keyExtractor) {
    return Comparator.comparing(keyExtractor.toFunction());
  }

  private static class ResultSetSpliterator extends Spliterators.AbstractSpliterator<SqlRow> {
    private final ResultSet rs;

    private ResultSetSpliterator(final ResultSet rs) {
      super(Long.MAX_VALUE, Spliterator.ORDERED);
      this.rs = rs;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super SqlRow> action) {
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
