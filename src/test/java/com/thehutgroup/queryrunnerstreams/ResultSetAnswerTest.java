package com.thehutgroup.queryrunnerstreams;

import static com.thehutgroup.queryrunnerstreams.ResultSetAnswer.doMockResultSet;
import static com.thehutgroup.queryrunnerstreams.ResultSetAnswer.mockResultSet;
import static com.thehutgroup.queryrunnerstreams.ResultSetAnswer.withMockResultSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResultSetAnswerTest {

  private static final String SQL_QUERY = "SELECT * FROM table WHERE value = ?";

  @Test
  @DisplayName("Test basic mockResultSet()")
  @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
  void mockResultSetTest() throws SQLException {

    final ResultSet rs = mockResultSet(
        new String[] { "name", "age" }, //columns
        new Object[][] {
            { "Alice",   20 },
            { "Bob",     35 },
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

    NamedParameterQueryRunner queryRunner = mock(NamedParameterQueryRunner.class);

    doMockResultSet(new String[] { "name", "age" }, //columns
        new Object[][] {
            { "Alice",   20 },
            { "Bob",     35 },
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
  @DisplayName("Test using when().thenAnswer(withMockResultSet())")
  @Disabled
  void whenDoAnswerWithMockResultSetTest() throws SQLException {

    NamedParameterQueryRunner queryRunner = mock(NamedParameterQueryRunner.class);

    final ResultSet rs = mockResultSet(
        new String[] { "name", "age" }, //columns
        new Object[][] {
            { "Alice",   20 },
            { "Bob",     35 },
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

  @Test
  @DisplayName("Test using doMockResultSet().when() on queryForList")
  void doMockResultSetQueryForListTest() throws SQLException {

    NamedParameterQueryRunner queryRunner = mock(NamedParameterQueryRunner.class);

    doMockResultSet(new String[] { "name", "age" }, //columns
        new Object[][] {
            { "Alice",   20 },
            { "Bob",     35 },
            { "Charles", 50 }
        })
        .when(queryRunner)
        .queryForList(eq(SQL_QUERY), any(), anyInt());

    List<String> names = queryRunner.queryForList(
        SQL_QUERY,
        row -> row.get("name", String.class),
        1);

    assertThat(names.size(), is(3));
    assertThat(names.get(0), is("Alice"));
    assertThat(names.get(1), is("Bob"));
    assertThat(names.get(2), is("Charles"));

  }

  @Test
  @DisplayName("Test using doMockResultSet().when() on stream")
  void testDoMockQueryStream() throws SQLException {
    NamedParameterQueryRunner queryRunner = mock(NamedParameterQueryRunner.class);

    doMockResultSet(
        new String[]{"Id", "Name", "Grade"},
        new Object[][]{
            { 1, "Steve", "A" },
            { 2, "Chris", "D" },
            { 3, "Bob",   "B" },
            { 4, "John",  "A" }}).when(queryRunner).stream(SQL_QUERY);

    List<Student> expectedStudents = new ArrayList<>();
    expectedStudents.add(new Student(1, "Steve", "A"));
    expectedStudents.add(new Student( 2, "Chris", "D"));
    expectedStudents.add(new Student(3, "Bob", "B"));
    expectedStudents.add(new Student(4, "John", "A"));

    List<Student> actualStudents = queryRunner.stream(SQL_QUERY)
        .map(row -> new Student(
            row.getInt("Id"),
            row.getString("Name"),
            row.getString("Grade")))
        .collect(Collectors.toList());

    assertThat(actualStudents, is(expectedStudents));
  }

  @Test
  @DisplayName("Test using doMockResultSet().when() on stream with empty result")
  void testDoMockQueryStreamWithEmptyResult() throws SQLException {
    NamedParameterQueryRunner queryRunner = mock(NamedParameterQueryRunner.class);

    doMockResultSet(
        new String[]{"Id", "Name", "Grade"},
        new Object[][]{ }).when(queryRunner).stream(SQL_QUERY);

    List<Student> expectedStudents = new ArrayList<>();

    List<Student> actualStudents = queryRunner.stream(SQL_QUERY)
        .map(row -> new Student(
            row.getInt("Id"),
            row.getString("Name"),
            row.getString("Grade")))
        .collect(Collectors.toList());

    assertThat(actualStudents, is(expectedStudents));
  }

  @Test
  @DisplayName("Test using doMockResultSet().when() on stream twice")
  void testDoMockQueryStreamWithEmptyResultTwice() throws SQLException {
    NamedParameterQueryRunner queryRunner = mock(NamedParameterQueryRunner.class);

    String key = "57";
    doMockResultSet(
        new String[]{"Id", "Name", "Grade"},
        new Object[][]{ }).when(queryRunner).stream(SQL_QUERY, key);

    Optional<Student> actualStudents = queryRunner.stream(SQL_QUERY, key)
        .map(row -> new Student(
            row.getInt("Id"),
            row.getString("Name"),
            row.getString("Grade")))
        .findAny();

    assertThat(actualStudents, is(Optional.empty()));

    Optional<Student> actualStudents2 = queryRunner.stream(SQL_QUERY, key)
        .map(row -> new Student(
            row.getInt("Id"),
            row.getString("Name"),
            row.getString("Grade")))
        .findAny();

    assertThat(actualStudents2, is(Optional.empty()));
  }

  @Test
  @DisplayName("Ensure that mock result set reacts as expect if it is closed")
  void testMockResultSetActsAsExpectedWhenClosed() throws SQLException {
    NamedParameterQueryRunner queryRunner = mock(NamedParameterQueryRunner.class);

    doMockResultSet(
        new String[]{"Id", "Name", "Grade"},
        new Object[][]{
            { 1, "Steve", "A" },
            { 2, "Chris", "D" },
            { 3, "Bob",   "B" },
            { 4, "John",  "A" }}).when(queryRunner).stream(SQL_QUERY);

    RuntimeSQLException ex = assertThrows(RuntimeSQLException.class, () -> queryRunner
        .stream(SQL_QUERY)
        .findFirst() //This should be an Optional<SqlRow> which is now closed
        .map(row -> row.getInt("Id"))
        .orElse(-1));

    assertThat(ex.getMessage(), is("Result set is closed"));
  }

  private static class Student {
    private final int id;
    private final String name;
    private final String grade;

    Student(final int id, final String name, final String grade) {
      this.id = id;
      this.name = name;
      this.grade = grade;
    }

    @Override
    public boolean equals(final Object that) {
      if (this == that) {
        return true;
      }
      if (that == null || getClass() != that.getClass()) {
        return false;
      }
      Student student = (Student) that;
      return id == student.id
          && Objects.equals(name, student.name)
          && Objects.equals(grade, student.grade);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, name, grade);
    }
  }
}
