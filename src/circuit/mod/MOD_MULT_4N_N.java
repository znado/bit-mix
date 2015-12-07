package circuit.mod;

import circuit.CompositeCircuit;


/**
 * Modular multiplication circuit that calculates XY mod M. Requires that X and Y are both currently less than M, and
 * additionally that the highest bit of M-1 is 1 (so M >= 2^(n-1)+1). Accepts X (the n-bit multiplicand), Y (the n-bit
 * multiplier), M-1 (the n-bit modulus), -M (aka invM, 2^n-M).
 */
public class MOD_MULT_4N_N extends CompositeCircuit {
  private final int bitLength;

  public MOD_MULT_4N_N(int n) {
    super(4 * n, n, n, "MOD_MULT_" + (4 * n) + "_" + n);
    if (n < 2) {
      throw new IllegalArgumentException("Modular multiplication requires at least 2 bits");
    }
    bitLength = n;
  }

  public static int X(int i) {
    return 4 * i;
  }

  public static int Y(int i) {
    return 4 * i + 1;
  }

  public static int invM(int i) {
    return 4 * i + 3;
  }

  public static int mMinusOne(int i) {
    return 4 * i + 2;
  }

  private static int stepCircuit(int i) {
    return i;
  }

  protected void createSubCircuits(final boolean isForGarbling) {
    for (int i = 0; i < bitLength; i++) {
      subCircuits[stepCircuit(i)] = new MOD_SUB_MULT_4Nplus1_N(bitLength);
    }
  }

  protected void connectWires() {
    for (int i = 0; i < bitLength; i++) {
      inputWires[Y(bitLength - 1 - i)].connectTo(subCircuits[stepCircuit(i)].inputWires, MOD_SUB_MULT_4Nplus1_N.Y_i());

      for (int circ = 0; circ < bitLength; circ++) {
        inputWires[X(i)].connectTo(subCircuits[stepCircuit(circ)].inputWires, MOD_SUB_MULT_4Nplus1_N.X(i));
        inputWires[mMinusOne(i)].connectTo(subCircuits[stepCircuit(circ)].inputWires,
            MOD_SUB_MULT_4Nplus1_N.mMinusOne(i));
        inputWires[invM(i)].connectTo(subCircuits[stepCircuit(circ)].inputWires, MOD_SUB_MULT_4Nplus1_N.invM(i));
        if (circ != bitLength - 1) {
          subCircuits[stepCircuit(circ)].outputWires[i].connectTo(subCircuits[stepCircuit(circ + 1)].inputWires,
              MOD_SUB_MULT_4Nplus1_N.A(i));
        }
      }
    }
  }

  protected void defineOutputWires() {
    System.arraycopy(subCircuits[stepCircuit(bitLength - 1)].outputWires, 0, outputWires, 0, bitLength);
  }

  protected void fixInternalWires() {
    for (int i = 0; i < bitLength; i++) {
      subCircuits[stepCircuit(0)].inputWires[MOD_SUB_MULT_4Nplus1_N.A(i)].fixWire(0);
    }
  }
}
