package test;

import main.FirstProtocol;

import java.math.BigInteger;
import java.util.Random;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public final class TestConstants {
  /**
   * Utility class - not meant to be instantiated.
   */
  private TestConstants() {}

  public static final BigInteger g = BigInteger.valueOf(2);
  public static final BigInteger m = BigInteger.probablePrime(FirstProtocol.BIT_LENGTH, new Random(100));
}
