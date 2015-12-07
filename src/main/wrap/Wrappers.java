package main.wrap;

import java.math.BigInteger;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public final class Wrappers {
  /**
   * Utility class - not meant to be instantiated.
   */
  private Wrappers() {}

  public static int X(int i) {
    return 2 * i;
  }

  public static int Y(int i) {
    return 2 * i + 1;
  }

  public static BigInteger result(BigInteger x, BigInteger y) {
    if (RUN_MULT.wasRun) {
      return x.multiply(y)
          .mod(AdditionCommon.MODULUS);
    }
    if (RUN_SQUARE.wasRun) {
      return x.modPow(BigInteger.valueOf(2), AdditionCommon.MODULUS);
    }
    if (RUN_ADD.wasRun || RUN_COND_ADD.wasRun) {
      return x.add(y)
          .mod(AdditionCommon.MODULUS);
    }
    if (RUN_DOUBLE.wasRun) {
      return x.add(x)
          .mod(AdditionCommon.MODULUS);
    }
    if (RUN_ADD1.wasRun) {
      return x.add(BigInteger.ONE)
          .mod(AdditionCommon.MODULUS);
    }
    if (RUN_EXP.wasRun) {
      return AdditionCommon.BASE.modPow(x, AdditionCommon.MODULUS);
    }
    throw new AssertionError("Unknown wrapper was run");
  }

  public static String resultString() {
    if (RUN_MULT.wasRun) {
      return "xy mod m";
    }
    if (RUN_SQUARE.wasRun) {
      return "x^2 mod m";
    }
    if (RUN_ADD.wasRun) {
      return "x+y mod m";
    }
    if (RUN_COND_ADD.wasRun) {
      return "x+?y mod m";
    }
    if (RUN_DOUBLE.wasRun) {
      return "2x mod m";
    }
    if (RUN_ADD1.wasRun) {
      return "x+1 mod m";
    }
    if (RUN_EXP.wasRun) {
      return "g^x mod m";
    }
    throw new AssertionError("Unknown wrapper was run");
  }
}
