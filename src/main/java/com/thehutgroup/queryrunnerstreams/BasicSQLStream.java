package com.thehutgroup.queryrunnerstreams;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class BasicSQLStream<T> {
  private final Stream<T> stream;

  private BasicSQLStream(Stream<T> stream) {
    this.stream = stream;
  }

  public Stream<T> stream() {
    return stream;
  }

  public <R> BasicSQLStream<R> map(SafeSQLFunction<? super T, ? extends R> mapper) {
    return new BasicSQLStream<R>(stream.map(mapper.toFunction()));
  }

  public <R> BasicSQLStream<R> flatMap(
      SafeSQLFunction<? super T, ? extends Stream<? extends R>> mapper){
    return new BasicSQLStream<R>(stream.flatMap(mapper.toFunction()));
  }

  public BasicSQLStream<T> filter(SafeSQLFunction<? super T, Boolean> predicate) {
    return new BasicSQLStream<T>(stream.filter(predicate.toPredicate()));
  }

  public boolean noneMatch(SafeSQLFunction<? super T, Boolean> predicate) {
    return stream.noneMatch(predicate.toPredicate());
  }

  public boolean allMatch(SafeSQLFunction<? super T, Boolean> predicate) {
    return stream.allMatch(predicate.toPredicate());
  }

  public boolean anyMatch(SafeSQLFunction<? super T, Boolean> predicate) {
    return stream.anyMatch(predicate.toPredicate());
  }

  public Optional<T> findAny() {
    return stream.findAny();
  }

  public Optional<T> findFirst() {
    return stream.findFirst();
  }

  public Optional<T> min(Comparator<? super T> comparator) {
    return stream.min(comparator);
  }

  public Optional<T> max(Comparator<? super T> comparator) {
    return stream.max(comparator);
  }

  public BasicSQLStream<T> sorted() {
    return new BasicSQLStream<>(stream.sorted());
  }

  public BasicSQLStream<T> sorted(Comparator<? super T> comparator) {
    return new BasicSQLStream<>(stream.sorted(comparator));
  }

  public <R> R collect(Supplier<R> supplier,
      SafeSQLBiConsumer<R, ? super T> accumulator,
      SafeSQLBiConsumer<R, R> combiner) {
    return stream.collect(supplier, accumulator.toBiConsumer(), combiner.toBiConsumer());
  }

  public <R, A> R collect(Collector<? super T, A, R> collector) {
    return stream.collect(collector);
  }

  public T reduce(T identity, SafeSQLBinaryOperator<T> accumulator) {
    return stream.reduce(identity, accumulator.toBinaryOperator());
  }

  public Optional<T> reduce(SafeSQLBinaryOperator<T> accumulator) {
    return stream.reduce(accumulator.toBinaryOperator());
  }

  public <U> U reduce(U identity, SafeSQLBiFunction<U, ? super T, U> accumulator,
      SafeSQLBinaryOperator<U> combiner) {
    return stream.reduce(identity, accumulator.toBiFunction(), combiner.toBinaryOperator());
  }

  public Object[] toArray() {
    return stream.toArray();
  }

  public <A> A[] toArray(IntFunction<A[]> generator) {
    return stream.toArray(generator);
  }

  public void forEach(SafeSQLConsumer<? super T> action) {
    stream.forEach(action.toConsumer());
  }
}
