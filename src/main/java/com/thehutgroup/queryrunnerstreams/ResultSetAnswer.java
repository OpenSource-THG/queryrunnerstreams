package com.thehutgroup.queryrunnerstreams;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.dbutils.ResultSetHandler;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

public class ResultSetAnswer<T> implements Answer<T> {

  private final Supplier<ResultSet> resultSetSupplier;

  private ResultSetAnswer(final ResultSet rs) {
    this.resultSetSupplier = () -> rs;
  }

  private ResultSetAnswer(final String[] columnNames, final Object[][] data) {
    this.resultSetSupplier = new MockResultSet(columnNames, data)::build;
  }

  @Override
  @SuppressFBWarnings("URV_INHERITED_METHOD_WITH_RELATED_TYPES")
  public T answer(final InvocationOnMock invocation) throws Throwable {

    ResultSet rs = resultSetSupplier.get();

    switch (invocation.getMethod().getName()) {
      case "queryForList":
        SafeSQLFunction<SqlRow, ?> rowMapper = Stream.of(invocation.getArguments())
            .filter(object -> object instanceof SafeSQLFunction)
            .map(object -> (SafeSQLFunction<SqlRow, ?>) object)
            .findFirst()
            .orElseThrow(() -> new Exception("No argument of type ResultSetHandler was passed"));

        List<Object> list = new ArrayList<>();

        while (rs.next()) {
          try {
            list.add(rowMapper.apply(new SqlRow(rs)));
          } catch (RuntimeSQLException ex) {
            throw ex.getParent();
          }
        }

        return (T) list;

      case "stream":
        return (T) QueryStream.of(rs, () -> {
          try {
            rs.close();
          } catch (SQLException ex) {
            throw new RuntimeSQLException(ex);
          }
        });

      default: //Methods based off query.
        return Stream.of(invocation.getArguments())
            .filter(object -> object instanceof ResultSetHandler)
            .map(object -> (ResultSetHandler<T>) object)
            .findFirst()
            .orElseThrow(() -> new Exception("No argument of type ResultSetHandler was passed"))
            .handle(rs);
    }

  }

  public static ResultSet mockResultSet(final String[] columnNames, final Object[][] data) {
    return new MockResultSet(columnNames, data).build();
  }

  public static <T> Answer<T> withMockResultSet(final ResultSet rs) {
    return new ResultSetAnswer<>(rs);
  }

  public static Stubber doMockResultSet(final ResultSet rs) {
    return doAnswer(new ResultSetAnswer(rs));
  }

  public static Stubber doMockResultSet(final String[] columnNames, final Object[][] data) {
    return doAnswer(new ResultSetAnswer(columnNames, data));
  }

  private static class MockResultSet {

    private final Map<String, Integer> columnIndices;
    private final Object[][] data;
    private int rowIndex;
    private Map<ResultSet, Boolean> closed = new HashMap<>();

    private MockResultSet(final String[] columnNames, final Object[][] data) {
      this.columnIndices = IntStream.range(0, columnNames.length).boxed()
          .collect(Collectors.toMap(k -> columnNames[k], Function.identity()));
      this.data = data;
      this.rowIndex = -1;
    }

    private ResultSet build() {
      try {
        final ResultSet rs = mock(ResultSet.class);

        // Mock rs.next()
        doAnswer(invocation -> ++rowIndex < data.length).when(rs).next();

        // Mock rs.close()
        doAnswer(this::close).when(rs).close();

        // Mock rs.getObject, getString etc
        doAnswer(this::getValue).when(rs).getObject(any());
        doAnswer(getAnswer(String.class)).when(rs).getString(any());
        doAnswer(getAnswer(Integer.class)).when(rs).getInt(any());
        doAnswer(getAnswer(Boolean.class)).when(rs).getBoolean(any());
        doAnswer(getAnswer(Timestamp.class)).when(rs).getTimestamp(any());
        doAnswer(getAnswer(BigDecimal.class)).when(rs).getBigDecimal(any());

        // Mock rs.getMetaData()
        final ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        doReturn(columnIndices.size()).when(metadata).getColumnCount();
        doReturn(metadata).when(rs).getMetaData();

        //Open this resultSet:
        closed.put(rs, false);

        return rs;
      } catch (SQLException ex) {
        throw new RuntimeSQLException(ex);
      }
    }

    @SuppressFBWarnings("UP_UNUSED_PARAMETER") //Parameter is for casting
    private <R> Answer getAnswer(final Class<R> clazz) {
      return invocation -> (R) getValue(invocation);
    }

    private Object getValue(final InvocationOnMock invocation) throws SQLException {

      if (isClosed(invocation)) {
        throw new SQLException("Result set is closed");
      }

      Object arg = invocation.getArgument(0);
      if (arg instanceof String) {
        return getValue((String) arg);
      } else {
        return getValue((int) arg);
      }
    }

    private Object getValue(final String columnName) {
      return getValue(columnIndices.get(columnName) + 1);
    }

    private Object getValue(final int columnIndex) {
      return data[rowIndex][columnIndex - 1];
    }

    private boolean isClosed(final ResultSet mock) {
      return closed.getOrDefault(mock, true);
    }

    private boolean isClosed(final InvocationOnMock invocation) {
      return isClosed((ResultSet) invocation.getMock());
    }

    private Object close(final InvocationOnMock invocation) throws SQLException {
      final ResultSet mock = (ResultSet) invocation.getMock();
      if (isClosed(mock)) {
        throw new SQLException("Result set has already been closed");
      }
      closed.put(mock, true);
      return null;
    }
  }
}
