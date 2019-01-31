package com.thehutgroup.queryrunnerstreams.test;

import static com.thehutgroup.queryrunnerstreams.ResultSetAnswer.doMockResultSet;
import static com.thehutgroup.queryrunnerstreams.ResultSetAnswer.mockResultSet;
import static com.thehutgroup.queryrunnerstreams.ResultSetAnswer.withMockResultSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thehutgroup.queryrunnerstreams.QueryStream;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.dbutils.QueryRunner;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ResultSetAnswerTest {

  private static final String SQL_QUERY = "SELECT * FROM table WHERE value = ?";

  @Test
  @DisplayName("Test basic mockResultSet()")
  @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
  void mockResultSetTest() throws SQLException {

    final ResultSet rs = mockResultSet(
        new String[] { "name", "age" }, //columns
        new Object[][] {
            { "Alice", 20 },
            { "Bob", 35 },
            { "Charles", 50 }
        });

    assertThat(rs.next(), is(true));
    assertThat(rs.getString("name"), is("Alice"));
    assertThat(rs.getInt("age"), is(20));

    assertThat(rs.next(), is(true));
    assertThat(rs.getString("name"), is("Bob"));
    assertThat(rs.getInt("age"), is(35));

    assertThat(rs.next(), is(true));
    assertThat(rs.getString("name"), is("Charles"));
    assertThat(rs.getInt("age"), is(50));

    assertThat(rs.next(), is(false));

  }

  @Test
  @DisplayName("Test using doMockResultSet().when()")
  void doMockResultSetTest() throws SQLException {

    QueryRunner queryRunner = mock(QueryRunner.class);

    doMockResultSet(new String[] { "name", "age" }, //columns
        new Object[][] {
            { "Alice", 20 },
            { "Bob", 35 },
            { "Charles", 50 }
        })
        .when(queryRunner)
        .query(eq(SQL_QUERY), any(), anyInt());

    List<String> names = queryRunner.query(
        SQL_QUERY,
        rs -> QueryStream.of(rs)
            .map(row -> row.get("name", String.class))
            .collect(Collectors.toList()),
        1);

    assertThat(names.size(), is(3));
    assertThat(names.get(0), is("Alice"));
    assertThat(names.get(1), is("Bob"));
    assertThat(names.get(2), is("Charles"));

  }

  @Test
  @Disabled
  @DisplayName("Test using when().thenAnswer(withMockResultSet())")
  void whenDoAnswerWithMockResultSetTest() throws SQLException {

    QueryRunner queryRunner = mock(QueryRunner.class);

    final ResultSet rs = mockResultSet(
        new String[] { "name", "age" }, //columns
        new Object[][] {
            { "Alice", 20 },
            { "Bob", 35 },
            { "Charles", 50 }
        });

    when(queryRunner.query(eq(SQL_QUERY), any(), anyInt())).thenAnswer(withMockResultSet(rs));

    List<String> names = queryRunner.query(
        SQL_QUERY,
        resultSet -> QueryStream.of(resultSet)
            .map(row -> row.get("name", String.class))
            .collect(Collectors.toList()),
        1);

    assertThat(names.size(), is(3));
    assertThat(names.get(0), is("Alice"));
    assertThat(names.get(1), is("Bob"));
    assertThat(names.get(2), is("Charles"));

  }

}
