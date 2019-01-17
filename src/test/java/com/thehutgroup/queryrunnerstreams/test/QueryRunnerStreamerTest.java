package com.thehutgroup.queryrunnerstreams.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.thehutgroup.queryrunnerstreams.QueryRunnerStreamer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueryRunnerStreamerTest {

  @Test
  @DisplayName("Test that a Stream can be made from a ResultSet")
  void testThatValuesCanBeExtracted() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    doReturn("value").when(rs).getObject("column");
    doReturn(7).when(rs).getRow();
    doReturn(true).when(rs).next();

    Optional<String> firstColumnValue = QueryRunnerStreamer.stream().handle(rs)
        .map(row -> row.get("column", String.class))
        .findFirst();

    assertThat(firstColumnValue.get(), is("value"));
  }

}
