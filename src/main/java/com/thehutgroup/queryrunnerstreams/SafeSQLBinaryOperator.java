package com.thehutgroup.queryrunnerstreams;


import java.util.function.BinaryOperator;

@FunctionalInterface
public interface SafeSQLBinaryOperator<T> extends SafeSQLBiFunction<T, T, T> {
  default BinaryOperator<T> toBinaryOperator() {
    return (inputT, inputU) -> toBiFunction().apply(inputT, inputU);
  }
}
