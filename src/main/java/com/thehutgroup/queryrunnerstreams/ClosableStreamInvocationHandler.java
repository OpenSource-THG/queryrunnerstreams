package com.thehutgroup.queryrunnerstreams;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClosableStreamInvocationHandler<T> implements InvocationHandler {

  private final Stream<T> base;
  private final Runnable onClose;

  public ClosableStreamInvocationHandler(Stream<T> base, Runnable onClose) {
    this.base = base;
    this.onClose = onClose;
  }

  @Override
  @SuppressFBWarnings //This method upsets Findbugs in a lot of ways
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {

      // If the method returns a standard stream, wrap it
      if (Stream.class.isAssignableFrom(method.getReturnType())) {
        return wrap((Stream) method.invoke(base, args), onClose);
      }

      // If it returns another type of stream, fully evaluate the stream (closing the connection)
      if (BaseStream.class.isAssignableFrom(method.getReturnType())) {
        return method.invoke(evaluateStream(), args);
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

  private Stream<T> evaluateStream() {
    try {
      return base.collect(Collectors.toList()).stream();
    } finally {
      close();
    }
  }

  private void close() {
    if (onClose != null) {
      onClose.run();
    }
  }

  public static <T> Stream<T> wrap(Stream<T> base, Runnable onClose) {
    return (Stream<T>) Proxy.newProxyInstance(
        null,
        new Class[] { Stream.class },
        new ClosableStreamInvocationHandler<T>(base, onClose));
  }
}
