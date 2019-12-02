package com.thehutgroup.queryrunnerstreams;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class ClosableStreamInvocationHandler<T> implements InvocationHandler {

  private final Stream<T> base;
  private final Runnable onClose;

  private ClosableStreamInvocationHandler(final Stream<T> base, final Runnable onClose) {
    this.base = base;
    this.onClose = onClose;
  }

  @Override
  @SuppressFBWarnings //This method upsets Findbugs in a lot of ways
  public Object invoke(final Object proxy, final Method method, final Object[] args)
      throws Throwable {
    try {

      // If the method returns a standard stream, wrap it
      if (Stream.class.isAssignableFrom(method.getReturnType())) {
        return wrap((Stream) method.invoke(base, args), onClose);
      }

      // If it returns another type of stream, fully evaluate the stream (closing the connection)
      if (BaseStream.class.isAssignableFrom(method.getReturnType())) {
        try {
          return evaluateStream((BaseStream) method.invoke(base, args));
        } finally {
          close();
        }
      }

      // Otherwise, evaluate the answer, then close the stream
      Object result = method.invoke(base, args);
      close();
      return result;

    } catch (Throwable ex) {
      try {
        close();
      } catch (Throwable closingEx) {
        //Worry about the first exception for now.
      }
      throw ex;
    }
  }

  private BaseStream evaluateStream(final BaseStream stream) {
    if (IntStream.class.isAssignableFrom(stream.getClass())) {
      return evaluateStream((IntStream) stream);
    }

    if (LongStream.class.isAssignableFrom(stream.getClass())) {
      return evaluateStream((LongStream) stream);
    }

    if (DoubleStream.class.isAssignableFrom(stream.getClass())) {
      return evaluateStream((DoubleStream) stream);
    }

    throw new RuntimeSQLException("Unable to handle a BaseStream of type " + stream.getClass());
  }

  private IntStream evaluateStream(final IntStream stream) {
    return stream.boxed().collect(Collectors.toList()).stream().mapToInt(Integer::intValue);
  }

  private LongStream evaluateStream(final LongStream stream) {
    return stream.boxed().collect(Collectors.toList()).stream().mapToLong(Long::longValue);
  }

  private DoubleStream evaluateStream(final DoubleStream stream) {
    return stream.boxed().collect(Collectors.toList()).stream().mapToDouble(Double::doubleValue);
  }

  private void close() {
    if (onClose != null) {
      onClose.run();
    }
  }

  public static <T> Stream<T> wrap(final Stream<T> base, final Runnable onClose) {
    return (Stream<T>) Proxy.newProxyInstance(
        null,
        new Class[] { Stream.class },
        new ClosableStreamInvocationHandler<T>(base, onClose));
  }
}
