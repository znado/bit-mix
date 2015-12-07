package util;

/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public final class Pair<A,B> {
  public final A first;
  public final B second;

  private Pair(A a, B b) {
    first = a;
    second = b;
  }

  public static <A,B> Pair<A,B> of(final A a, final B b) {
    return new Pair<>(a, b);
  }
}
