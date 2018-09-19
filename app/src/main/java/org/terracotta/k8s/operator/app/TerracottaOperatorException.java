package org.terracotta.k8s.operator.app;

public class TerracottaOperatorException extends RuntimeException {
  public TerracottaOperatorException(String s) {
    super(s);
  }

  public TerracottaOperatorException(String s, Throwable e) {
    super(s, e);
  }
}
