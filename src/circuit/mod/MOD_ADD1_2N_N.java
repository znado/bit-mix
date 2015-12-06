package circuit.mod;

import circuit.CompositeCircuit;
import circuit.core.MUX_2Lplus1_L;
import circuit.core.bool.ADD_3_2;
import circuit.core.bool.OR_2_1;
import circuit.core.bool.XOR_2_1;


/**
 * Modular addition circuit which calculates A+1 mod M, where M-1 must have its top bit set to 1. Takes A (an n-bit
 * integer less than M), and M-1 (where M is an n-bit integer which is at least 2^(n-1)+1). More efficient than using
 * MOD_ADD_4N_N since A+1 mod M can either be A+1 (if A != M-1) or 0 (if A == M-1).
 */
public class MOD_ADD1_2N_N extends CompositeCircuit {
  private final int bitLength;

  public MOD_ADD1_2N_N(int n) {
    super(2 * n, n, 3 * n + 1, "MOD_ADD1_" + (2 * n) + "_" + n);
    bitLength = n;
  }

  public static int A(int i) {
    return 2 * i;
  }

  private static int adder(int i) {
    return 3 * i;
  }

  private static int diff(int i) {
    return 3 * i + 2;
  }

  public static int mMinusOne(int i) {
    return 2 * i + 1;
  }

  private static int neq(int i) {
    return 3 * i + 1;
  }

  protected void createSubCircuits() throws Exception {
    for (int i = 0; i < bitLength; i++) {
      subCircuits[adder(i)] = new ADD_3_2();
      subCircuits[neq(i)] = new XOR_2_1();
      subCircuits[diff(i)] = OR_2_1.newInstance();
    }
    subCircuits[3 * bitLength] = new MUX_2Lplus1_L(bitLength);

    super.createSubCircuits();
  }

  protected void connectWires() {
    for (int i = 0; i < bitLength; i++) {
      inputWire(A(i)).connectTo(subCircuits[adder(i)].inputWires, ADD_3_2.X);
      inputWire(A(i)).connectTo(subCircuits[neq(i)].inputWires, LEFT_INPUT);
      inputWire(mMinusOne(i)).connectTo(subCircuits[neq(i)].inputWires, RIGHT_INPUT);
      subCircuits[neq(i)].outputWire()
          .connectTo(subCircuits[diff(i)].inputWires, LEFT_INPUT);
      subCircuits[(adder(i))].outputWire(ADD_3_2.S)
          .connectTo(subCircuits[3 * bitLength].inputWires, MUX_2Lplus1_L.Y(i));

      if (i != 0) {
        subCircuits[adder(i - 1)].outputWire(ADD_3_2.COUT)
            .connectTo(subCircuits[adder(i)].inputWires, ADD_3_2.CIN);
        subCircuits[diff(i - 1)].outputWire()
            .connectTo(subCircuits[diff(i)].inputWires, RIGHT_INPUT);
      }
    }
    subCircuits[diff(bitLength - 1)].outputWire()
        .connectTo(subCircuits[3 * bitLength].inputWires, MUX_2Lplus1_L.C(bitLength));
    // C will be 0 if A == M-1, so SELECT will return MUX.X (fixed to be 0)
  }

  protected void defineOutputWires() {
    System.arraycopy(subCircuits[3 * bitLength].outputWires, 0, outputWires, 0, bitLength);
  }

  protected void fixInternalWires() {
    for (int i = 0; i < bitLength; i++) {
      subCircuits[3 * bitLength].inputWire(MUX_2Lplus1_L.X(i))
          .fixWire(0);
      subCircuits[adder(i)].inputWire(ADD_3_2.Y)
          .fixWire(0);
    }
    subCircuits[adder(0)].inputWire(ADD_3_2.CIN)
        .fixWire(1);
    subCircuits[diff(0)].inputWire(RIGHT_INPUT)
        .fixWire(0);
  }
}
