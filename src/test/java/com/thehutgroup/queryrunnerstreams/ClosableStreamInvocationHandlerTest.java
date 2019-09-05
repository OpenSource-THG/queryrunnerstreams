package com.thehutgroup.queryrunnerstreams;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClosableStreamInvocationHandlerTest {

  private boolean closed = false;

  @BeforeEach
  void setClosedToFalse() {
    closed = false;
  }

  @Test
  @DisplayName("Check that findAny() closes the connection")
  void testFindAny() {

    Optional<Integer> value = ClosableStreamInvocationHandler
        .wrap(Stream.of(1, 2, 3), this::close)
        .map(x -> x * 2)
        .filter(x -> x % 6 == 0)
        .findAny();

    assertThat(closed, is(true));
    assertThat(value, is(Optional.of(6)));
  }

  @Test
  @DisplayName("Check that collect() closes the connection")
  void testCollect() {

    String value = ClosableStreamInvocationHandler
        .wrap(Stream.of(1, 2, 3), this::close)
        .map(x -> x * 2)
        .map(Object::toString)
        .collect(Collectors.joining());

    assertThat(closed, is(true));
    assertThat(value, is("246"));
  }

  @Test
  @DisplayName("Check that converting to an IntStream closes the connection")
  void testNoOtherStreams() {

    OptionalInt value = ClosableStreamInvocationHandler
        .wrap(Stream.of(1, 2, 3), this::close)
        .mapToInt(x -> x * 2)
        .findFirst();

    assertThat(closed, is(true));
    assertThat(value, is(OptionalInt.of(2)));
  }

  private void close() {
    this.closed = true;
  }
}
