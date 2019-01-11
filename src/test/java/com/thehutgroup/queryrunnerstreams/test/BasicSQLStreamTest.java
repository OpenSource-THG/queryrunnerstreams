package com.thehutgroup.queryrunnerstreams.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.thehutgroup.queryrunnerstreams.BasicSQLStream;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BasicSQLStreamTest {


  @Test
  @DisplayName("Check that the Stream.map() implementation works as expected")
  void checkThatFilterWorksAsExpected() {

    String result = new BasicSQLStream<>(Stream.of(1, 2, 3))
        .map(val -> val * 2)
        .map(Object::toString)
        .collect(Collectors.joining(","));

    assertThat(result, is("2,4,6"));
  }

  @Test
  @DisplayName("Check that the Stream.filter() implementation works as expected")
  void checkThatMapWorksAsExpected() {

    String result = new BasicSQLStream<>(Stream.of(1, 2, 3))
        .filter(val -> val % 2 == 1)
        .map(Object::toString)
        .collect(Collectors.joining(","));

    assertThat(result, is("1,3"));
  }

  @Test
  @DisplayName("Check that the Stream.###Match() implementations works as expected")
  void checkThatMatchWorksAsExpected() {

    assertThat(new BasicSQLStream<>(Stream.of(1, 2, 3)).allMatch(x -> x == 1), is(false));
    assertThat(new BasicSQLStream<>(Stream.of(1, 2, 3)).allMatch(x -> x <= 4), is(true));

    assertThat(new BasicSQLStream<>(Stream.of(1, 2, 3)).anyMatch(x -> x == 1), is(true));
    assertThat(new BasicSQLStream<>(Stream.of(1, 2, 3)).anyMatch(x -> x >= 4), is(false));

    assertThat(new BasicSQLStream<>(Stream.of(1, 2, 3)).noneMatch(x -> x < 4), is(false));
    assertThat(new BasicSQLStream<>(Stream.of(1, 2, 3)).noneMatch(x -> x > 4), is(true));
  }

  @Test
  @DisplayName("Check that the Stream.findFirst() implementation works as expected")
  void checkThatFindFirstWorksAsExpected() {

    assertThat(new BasicSQLStream<>(Stream.of(1, 2, 3)).findFirst(), is(Optional.of(1)));
    assertThat(new BasicSQLStream<>(Stream.of(1, 2, 3)).findAny().isPresent(), is(true));
  }

  @Test
  @DisplayName("Check that Stream.map() and Stream.filter() methods accept SQL Exceptions")
  void checkThatMapAndFilterAllowSQLExceptions() {

    String result = new BasicSQLStream<>(Stream.of(1, 2, 3))
        .map(this::plusOne)
        .filter(this::isOdd)
        .map(this::plusOne)
        .map(Object::toString)
        .collect(Collectors.joining(","));

    assertThat(result, is("4"));
  }

  private int plusOne(int val) throws SQLException {
    if (val == 0) {
      throw new SQLException();
    }
    return val + 1;
  }

  private boolean isOdd(int val) throws SQLException {
    if (val == 0) {
      throw new SQLException();
    }
    return val % 2 != 0;
  }
}
