package circuit.mod;

import circuit.CompositeCircuit;
import circuit.Wire;
import circuit.arith.ADD_2L_L;
import circuit.core.MUX_2Lplus1_L;
import circuit.core.bool.AND_2_1;
import circuit.core.bool.XOR_2_1;


/**
 * Calculates X^2 mod M for some X < M and where M-1 has a top bit of 1 (i.e. M > 2^(n-1)+1). Takes X, M-1, and invM.
 *
 * Saves as much time as possible by using non-modular operations in every place where no inputs can cause an overflow,
 * specifically whenever adding or doubling things which will never be n bits.
 *
 * @author nschank
 * @version 2.1
 */
public class MOD_SQUARE_3N_N extends CompositeCircuit {
  private static final int A2_1_2a = 6;
  private static final int A2_1_2b = 7;
  private static final int A2_1_3 = 5;
  private static final boolean DEBUG_MODE = false;
  private static final int QN2_ADD1 = 4;
  private static final int QN2_DOUBLE_1 = 2;
  private static final int QN2_DOUBLE_2 = 3;
  private static final int QN3_ADD1 = 1;
  private static final int QN3_DOUBLE = 0;
  private final int bitLength;
  private final int maxSafeK;
  private final int numSafeK;
  private final int safeSubcircuitIndex;
  private final int unsafeSubcircuitIndex;

  public MOD_SQUARE_3N_N(int n) {
    super(3 * n, n, A2_1_2b + 1 + 2 * ((n - 3) / 2 - 1) + 4 * (n / 2 + 1), "x^2 mod_" + (3 * n) + "_" + n);
    if (n < 7) {
      throw new IllegalArgumentException("Bitlength must be at least 7 when squaring");
    }
    if (n % 2 != 0) {
      throw new IllegalArgumentException("Bitlength must be even due to an unidentified bug");
    }
    bitLength = n;
    maxSafeK = (n - 3) / 2;
    numSafeK = maxSafeK - 1;
    safeSubcircuitIndex = A2_1_2b + 1;
    unsafeSubcircuitIndex = safeSubcircuitIndex + 2 * numSafeK;
  }

  public static int X(int i) {
    return 3 * i;
  }

  public static int invM(int i) {
    return 3 * i + 2;
  }

  public static int mMinusOne(int i) {
    return 3 * i + 1;
  }

  /**
   * Returns the ith bit of a_k^2, which has either length 2*(k+1) for a safe k, or length n for an unsafe k. The final
   * output of this circuit will be a_{n-1}^2. (since a_{n-1} is in fact X).
   */
  private Wire a_2_k(int k, int i) {
    checkBetween(1, k, bitLength - 1);
    checkBetween(0, i, Math.min(2 * (k + 1), bitLength) - 1);
    if (k == 1) {
      if (i == 3) {
        return subCircuits[A2_1_3].outputWire();
      } else if (i == 2) {
        return subCircuits[A2_1_2b].outputWire();
      } else if (i == 0) {
        return a_k(1, 0);
      } else {
        throw new IllegalArgumentException("a_1^2 bit 1 is fixed");
      }
    }
    return subCircuits[selector(k)].outputWire(i);
  }

  /**
   * Returns the ith bit of a_k, where a_k is the most significant k+1 bits of X
   */
  private Wire a_k(int k, int i) {
    checkBetween(1, k, bitLength - 2);
    checkBetween(0, i, k);
    int k0 = bitLength - (k + 1);
    return inputWire(X(k0 + i));
  }

  /**
   * Returns the index of the subcircuit which adds together 4a_{k-1}^2 and q_{k-1}. The output of this circuit should
   * be fed into the final selector for a_k^2.
   *
   * @param k
   *     The index of the a_k^2 being calculated
   *
   * @return The index of the adder subcircuit
   */
  private int adder(int k) {
    check(k);
    if (k <= maxSafeK) {
      int i = k - 2;
      return safeSubcircuitIndex + 2 * i;
    } else {
      return firstDouble(k) + 2;
    }
  }

  /**
   * Returns the index of the ith wire of the left summand in adder(k). The left summand of adder k should be
   * 4a_{k-1}^2.
   */
  private int adderLeft(int k, int i) {
    check(k);
    checkBetween(0, i, Math.min(2 * k + 2, bitLength) - 1);
    if (k <= maxSafeK) {
      return ADD_2L_L.X(i);
    }
    return MOD_ADD_4N_N.A(i);
  }

  /**
   * Returns the index of the ith wire of the right summand in adder(k). The right summand of adder k should be
   * q_{k-1}.
   */
  private int adderRight(int k, int i) {
    check(k);
    checkBetween(0, i, Math.min(2 * k + 2, bitLength) - 1);
    if (k <= maxSafeK) {
      return ADD_2L_L.Y(i);
    }
    return MOD_ADD_4N_N.B(i);
  }

  /**
   * Tbe bit b_k is the LSB of a_k. a_k^2 = 4a_{k-1}^2 + b_kq_{k-1}.
   */
  private Wire b_k(int k) {
    checkBetween(2, k, bitLength - 1);
    return inputWire(X(bitLength - k - 1));
  }

  private void check(int k) {
    if (DEBUG_MODE) {
      if (k < 2 || k > bitLength - 1) {
        throw new IndexOutOfBoundsException("k");
      }
    }
  }

  private void checkBetween(int left, int i, int right) {
    if (DEBUG_MODE) {
      if (left > i || right < i) {
        throw new IndexOutOfBoundsException("i not between " + left + ", " + right);
      }
    }
  }

  protected void createSubCircuits(final boolean isForGarbling) {
    subCircuits[QN3_DOUBLE] = new MOD_DOUBLE_3N_N(bitLength);
    subCircuits[QN2_DOUBLE_1] = new MOD_DOUBLE_3N_N(bitLength);
    subCircuits[QN2_DOUBLE_2] = new MOD_DOUBLE_3N_N(bitLength);

    subCircuits[QN3_ADD1] = new MOD_ADD1_2N_N(bitLength);
    subCircuits[QN2_ADD1] = new MOD_ADD1_2N_N(bitLength);

    subCircuits[A2_1_3] = AND_2_1.newInstance(isForGarbling);
    subCircuits[A2_1_2a] = new XOR_2_1();
    subCircuits[A2_1_2b] = AND_2_1.newInstance(isForGarbling);

    for (int k = 2; k <= maxSafeK; k++) {
      subCircuits[adder(k)] = new ADD_2L_L(2 * k + 2);
      subCircuits[selector(k)] = new MUX_2Lplus1_L(2 * k + 2);
    }
    for (int k = maxSafeK + 1; k < bitLength; k++) {
      subCircuits[adder(k)] = new MOD_ADD_4N_N(bitLength);
      subCircuits[selector(k)] = new MUX_2Lplus1_L(bitLength);
      subCircuits[firstDouble(k)] = new MOD_DOUBLE_3N_N(bitLength);
      subCircuits[secondDouble(k)] = new MOD_DOUBLE_3N_N(bitLength);
    }
  }

  protected void connectWires() {
    // Plug in all modular circuits
    for (int i = 0; i < bitLength; i++) {
      inputWire(mMinusOne(i)).connectTo(subCircuits[QN3_DOUBLE].inputWires, MOD_DOUBLE_3N_N.mMinusOne(i));
      inputWire(mMinusOne(i)).connectTo(subCircuits[QN2_DOUBLE_1].inputWires, MOD_DOUBLE_3N_N.mMinusOne(i));
      inputWire(mMinusOne(i)).connectTo(subCircuits[QN2_DOUBLE_2].inputWires, MOD_DOUBLE_3N_N.mMinusOne(i));
      inputWire(invM(i)).connectTo(subCircuits[QN3_DOUBLE].inputWires, MOD_DOUBLE_3N_N.invM(i));
      inputWire(invM(i)).connectTo(subCircuits[QN2_DOUBLE_1].inputWires, MOD_DOUBLE_3N_N.invM(i));
      inputWire(invM(i)).connectTo(subCircuits[QN2_DOUBLE_2].inputWires, MOD_DOUBLE_3N_N.invM(i));

      inputWire(mMinusOne(i)).connectTo(subCircuits[QN3_ADD1].inputWires, MOD_ADD1_2N_N.mMinusOne(i));
      inputWire(mMinusOne(i)).connectTo(subCircuits[QN2_ADD1].inputWires, MOD_ADD1_2N_N.mMinusOne(i));

      for (int k = maxSafeK + 1; k < bitLength; k++) {
        inputWire(mMinusOne(i)).connectTo(subCircuits[firstDouble(k)].inputWires, MOD_DOUBLE_3N_N.mMinusOne(i));
        inputWire(mMinusOne(i)).connectTo(subCircuits[secondDouble(k)].inputWires, MOD_DOUBLE_3N_N.mMinusOne(i));
        inputWire(invM(i)).connectTo(subCircuits[firstDouble(k)].inputWires, MOD_DOUBLE_3N_N.invM(i));
        inputWire(invM(i)).connectTo(subCircuits[secondDouble(k)].inputWires, MOD_DOUBLE_3N_N.invM(i));

        inputWire(mMinusOne(i)).connectTo(subCircuits[adder(k)].inputWires, MOD_ADD_4N_N.mMinusOne(i));
        inputWire(invM(i)).connectTo(subCircuits[adder(k)].inputWires, MOD_ADD_4N_N.invM(i));
      }
    }

    // Plug into all selectors
    for (int k = 2; k < bitLength; k++) {
      b_k(k).connectTo(subCircuits[selector(k)].inputWires, MUX_2Lplus1_L.C(Math.min(2 * k + 2, bitLength)));
    }

    // Calculate q_{n-2,n-3}
    for (int i = 0; i < bitLength - 2; i++) {
      // bit 0 and bit n-1 are fixed to 0
      a_k(bitLength - 3, i).connectTo(subCircuits[QN3_DOUBLE].inputWires, MOD_DOUBLE_3N_N.A(i + 1));
    }
    for (int i = 0; i < bitLength - 1; i++) {
      a_k(bitLength - 2, i).connectTo(subCircuits[QN2_DOUBLE_1].inputWires, MOD_DOUBLE_3N_N.A(i));
      // bit n-1 is fixed to 0
    }
    for (int i = 0; i < bitLength; i++) {
      subCircuits[QN3_DOUBLE].outputWire(i)
          .connectTo(subCircuits[QN3_ADD1].inputWires, MOD_ADD1_2N_N.A(i));
      subCircuits[QN2_DOUBLE_1].outputWire(i)
          .connectTo(subCircuits[QN2_DOUBLE_2].inputWires, MOD_DOUBLE_3N_N.A(i));
      subCircuits[QN2_DOUBLE_2].outputWire(i)
          .connectTo(subCircuits[QN2_ADD1].inputWires, MOD_ADD1_2N_N.A(i));
    }

    // Calculate a_1^2
    a_k(1, 1).connectTo(subCircuits[A2_1_3].inputWires, LEFT_INPUT);
    a_k(1, 0).connectTo(subCircuits[A2_1_3].inputWires, RIGHT_INPUT);

    a_k(1, 0).connectTo(subCircuits[A2_1_2a].inputWires, LEFT_INPUT);
    // RIGHT is fixed to 1

    subCircuits[A2_1_2a].outputWire()
        .connectTo(subCircuits[A2_1_2b].inputWires, LEFT_INPUT);
    a_k(1, 1).connectTo(subCircuits[A2_1_2b].inputWires, RIGHT_INPUT);

    // Connect q_k to all adders
    for (int k = 1; k < bitLength - 3; k++) {
      // Bits 0 and 1 are fixed
      for (int i = 2; i < k + 3; i++) {
        q_k(k, i).connectTo(subCircuits[adder(k + 1)].inputWires, adderRight(k + 1, i));
      }
      // Bits >= k+3 are fixed
    }
    for (int k = bitLength - 3; k < bitLength - 1; k++) {
      for (int i = 0; i < bitLength; i++) {
        q_k(k, i).connectTo(subCircuits[adder(k + 1)].inputWires, adderRight(k + 1, i));
      }
    }

    // Connect adders to selectors
    for (int k = 2; k < bitLength; k++) {
      for (int i = 0; i < Math.min(bitLength, 2 * k + 2); i++) {
        subCircuits[adder(k)].outputWire(i)
            .connectTo(subCircuits[selector(k)].inputWires, selectorRight(k, i));
      }
    }

    // a_1^2 is a special case
    // bits 0, 1, and 3 are fixed to 0
    a_2_k(1, 0).connectTo(subCircuits[adder(2)].inputWires, adderLeft(2, 2));
    a_2_k(1, 2).connectTo(subCircuits[adder(2)].inputWires, adderLeft(2, 4));
    a_2_k(1, 3).connectTo(subCircuits[adder(2)].inputWires, adderLeft(2, 5));
    a_2_k(1, 0).connectTo(subCircuits[selector(2)].inputWires, selectorLeft(2, 2));
    a_2_k(1, 2).connectTo(subCircuits[selector(2)].inputWires, selectorLeft(2, 4));
    a_2_k(1, 3).connectTo(subCircuits[selector(2)].inputWires, selectorLeft(2, 5));

    // Connect a_k^2 to adders and selectors, safe k's
    for (int k = 2; k <= maxSafeK; k++) {
      for (int i = 0; i < 2 * k + 2; i++) {
        // Bottom two bits are fixed
        a_2_k(k, i).connectTo(subCircuits[adder(k + 1)].inputWires, adderLeft(k + 1, i + 2));
        a_2_k(k, i).connectTo(subCircuits[selector(k + 1)].inputWires, selectorLeft(k + 1, i + 2));
        // Top bits of a_maxSafeK^2 are fixed
      }
    }

    for (int k = maxSafeK + 1; k < bitLength - 1; k++) {
      for (int i = 0; i < bitLength; i++) {
        a_2_k(k, i).connectTo(subCircuits[firstDouble(k + 1)].inputWires, MOD_DOUBLE_3N_N.A(i));
        subCircuits[firstDouble(k + 1)].outputWire(i)
            .connectTo(subCircuits[secondDouble(k + 1)].inputWires, MOD_DOUBLE_3N_N.A(i));
        subCircuits[secondDouble(k + 1)].outputWire(i)
            .connectTo(subCircuits[adder(k + 1)].inputWires, adderLeft(k + 1, i));
        subCircuits[secondDouble(k + 1)].outputWire(i)
            .connectTo(subCircuits[selector(k + 1)].inputWires, selectorLeft(k + 1, i));
      }
    }
  }

  protected void defineOutputWires() {
    System.arraycopy(subCircuits[selector(bitLength - 1)].outputWires, 0, outputWires, 0, bitLength);
  }

  protected void fixInternalWires() {
    // Fix the top bit of 2a_{n-1}
    subCircuits[QN3_DOUBLE].inputWire(MOD_DOUBLE_3N_N.A(0))
        .fixWire(0);
    subCircuits[QN3_DOUBLE].inputWire(MOD_DOUBLE_3N_N.A(bitLength - 1))
        .fixWire(0);
    subCircuits[QN2_DOUBLE_1].inputWire(MOD_DOUBLE_3N_N.A(bitLength - 1))
        .fixWire(0);
    // Negate a_2 bit 0
    subCircuits[A2_1_2a].inputWire(RIGHT_INPUT)
        .fixWire(1);

    // Fix the bottom two and top (2(k+2) - (k+3)) bits are fixed
    for (int k = 1; k < bitLength - 3; k++) {
      subCircuits[adder(k + 1)].inputWire(adderRight(k + 1, 0))
          .fixWire(1);
      subCircuits[adder(k + 1)].inputWire(adderRight(k + 1, 1))
          .fixWire(0);
      for (int i = k + 3; i < Math.min(2 * (k + 2), bitLength); i++) {
        subCircuits[adder(k + 1)].inputWire(adderRight(k + 1, i))
            .fixWire(0);
      }
    }

    // Fixes the bits of a_1^2
    subCircuits[adder(2)].inputWire(adderLeft(2, 0))
        .fixWire(0);
    subCircuits[adder(2)].inputWire(adderLeft(2, 1))
        .fixWire(0);
    subCircuits[adder(2)].inputWire(adderLeft(2, 3))
        .fixWire(0);
    subCircuits[selector(2)].inputWire(selectorLeft(2, 0))
        .fixWire(0);
    subCircuits[selector(2)].inputWire(selectorLeft(2, 1))
        .fixWire(0);
    subCircuits[selector(2)].inputWire(selectorLeft(2, 3))
        .fixWire(0);

    // Fixes bits of 4a_k^2 for safe k's
    for (int k = 2; k <= maxSafeK; k++) {
      subCircuits[adder(k + 1)].inputWire(adderLeft(k + 1, 0))
          .fixWire(0);
      subCircuits[adder(k + 1)].inputWire(adderLeft(k + 1, 1))
          .fixWire(0);
      subCircuits[selector(k + 1)].inputWire(selectorLeft(k + 1, 0))
          .fixWire(0);
      subCircuits[selector(k + 1)].inputWire(selectorLeft(k + 1, 1))
          .fixWire(0);
    }
  }

  /**
   * Returns the subcircuit index of the first doubler constructing a_k^2, which is calculating 2a_{k-1}^2.
   */
  private int firstDouble(int k) {
    checkBetween(maxSafeK + 1, k, bitLength - 1);
    int i = k - 2 - numSafeK;
    return unsafeSubcircuitIndex + 4 * i;
  }

  /**
   * The value q_k is 4a_k+1. For k in [1, bitlength - 4] q_k is k+3 bits long, and its bottom two bits are fixed. For
   * bitlength - 3..2, it is bitLength bits long, and no bits are fixed.
   */
  private Wire q_k(int k, int i) {
    checkBetween(1, k, bitLength - 2);
    if (k < bitLength - 3) {
      checkBetween(2, i, k + 2);
      return a_k(k, i - 2);
    }
    checkBetween(0, i, bitLength - 1);
    if (k == bitLength - 3) {
      return subCircuits[QN3_ADD1].outputWire(i);
    }
    return subCircuits[QN2_ADD1].outputWire(i);
  }

  /**
   * Returns the subcircuit index of the second doubler constructing a_k^2, which is calculating 4a_{k-1}^2.
   */
  private int secondDouble(int k) {
    checkBetween(maxSafeK + 1, k, bitLength - 1);
    return firstDouble(k) + 1;
  }

  /**
   * The subcircuit index of the selector circuit, which outputs a_k^2 by choosing between opt_0=4a_{k-1}^2 or opt_1 =
   * opt_0 + q_{k-1}. The output of the entire squaring circuit is the output wires of selector(bitLength).
   *
   * @param k
   *     The index of the a_k^2 being calculated
   *
   * @return The index in subCircuits[]
   */
  private int selector(int k) {
    check(k);
    if (k <= maxSafeK) {
      return adder(k) + 1;
    } else {
      return firstDouble(k) + 3;
    }
  }

  /**
   * Returns the index of the ith input bit of 4a_{k-1}^2 into the selector building a_k^2
   */
  private int selectorLeft(int k, int i) {
    check(k);
    checkBetween(0, i, Math.min(2 * k + 2, bitLength) - 1);
    return MUX_2Lplus1_L.X(i);
  }

  /**
   * Returns the index of the ith input bit of 4a_{k-1}^2+(q_{k-1}) into the selector building a_k^2
   */
  private int selectorRight(int k, int i) {
    check(k);
    checkBetween(0, i, Math.min(2 * (k + 1), bitLength) - 1);
    return MUX_2Lplus1_L.Y(i);
  }
}
