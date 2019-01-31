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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.dbutils.ResultSetHandler;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

public class ResultSetAnswer<T> implements Answer<T> {

  private final ResultSet rs;

  private ResultSetAnswer(ResultSet rs) {
    this.rs = rs;
  }

  private ResultSetAnswer(final String[] columnNames, final Object[][] data) {
    this.rs = new MockResultSet(columnNames, data).build();
  }

  @Override
  public T answer(InvocationOnMock invocation) throws Throwable {
    return Stream.of(invocation.getArguments())
        .filter(object -> object instanceof ResultSetHandler)
        .map(object -> (ResultSetHandler<T>) object)
        .findFirst()
        .orElseThrow(() -> new Exception("No argument of type ResultSetHandler was passed"))
        .handle(rs);
  }

  public static ResultSet mockResultSet(final String[] columnNames, final Object[][] data) {
    return new MockResultSet(columnNames, data).build();
  }

  public static <T> Answer<T> withMockResultSet(ResultSet rs) {
    return new ResultSetAnswer<>(rs);
  }

  public static Stubber doMockResultSet(ResultSet rs) {
    return doAnswer(new ResultSetAnswer(rs));
  }

  public static Stubber doMockResultSet(final String[] columnNames, final Object[][] data) {
    return doAnswer(new ResultSetAnswer(columnNames, data));
  }

  private static class MockResultSet {

    private final Map<String, Integer> columnIndices;
    private final Object[][] data;
    private int rowIndex;

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

        return rs;
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
    }

    @SuppressFBWarnings("UP_UNUSED_PARAMETER") //Parameter is for casting
    private <R> Answer getAnswer(Class<R> clazz) {
      return invocation -> (R) getValue(invocation);
    }

    private Object getValue(InvocationOnMock invocation) {
      Object arg = invocation.getArgument(0);
      if (arg instanceof String) {
        return getValue((String) arg);
      } else {
        return getValue((int) arg);
      }
    }

    private Object getValue(String columnName) {
      return getValue(columnIndices.get(columnName) + 1);
    }

    private Object getValue(int columnIndex) {
      return data[rowIndex][columnIndex - 1];
    }
  }
}
