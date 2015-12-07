// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package circuit.mod;

import circuit.CompositeCircuit;
import circuit.core.MUX_2Lplus1_L;
import circuit.core.bool.ADD_3_2;
import circuit.core.bool.GT_3_1;
import circuit.core.bool.OR_2_1;


/**
 * Modular doubling circuit which calculates A+B mod M, where M-1 must have its top bit set to 1. Takes A (an n-bit
 * integer less than M), M-1 (where M is an n-bit integer which is at least 2^(n-1)+1), and invM (n bits, -M mod 2^n)
 */
public class MOD_DOUBLE_3N_N extends CompositeCircuit {
  private final int bitLength;

  public MOD_DOUBLE_3N_N(int n) {
    super(3 * n, n, 2 * n + 2, "MOD_DOUBLE_" + (3 * n) + "_" + n);
    bitLength = n;
  }

  public static int A(int i) {
    return 3 * i;
  }

  public static int invM(int i) {
    return 3 * i + 1;
  }

  public static int mMinusOne(int i) {
    return 3 * i + 2;
  }

  protected void createSubCircuits(final boolean isForGarbling) {
    for (int i = 0; i < bitLength; i++) {
      subCircuits[fixedAdder(i)] = new ADD_3_2();
      subCircuits[unfixedGt(i)] = new GT_3_1();
    }
    subCircuits[2 * bitLength] = OR_2_1.newInstance(isForGarbling);
    subCircuits[2 * bitLength + 1] = new MUX_2Lplus1_L(bitLength);
  }

  protected void connectWires() {
    for (int i = 0; i < bitLength; i++) {
      inputWire(invM(i)).connectTo(subCircuits[fixedAdder(i)].inputWires, ADD_3_2.X);
      inputWire(mMinusOne(i)).connectTo(subCircuits[unfixedGt(i)].inputWires, GT_3_1.Y);

      subCircuits[fixedAdder(i)].outputWire(ADD_3_2.S)
          .connectTo(subCircuits[2 * bitLength + 1].inputWires, MUX_2Lplus1_L.Y(i));

      if (i != 0) {
        inputWire(A(i - 1)).connectTo(subCircuits[fixedAdder(i)].inputWires, ADD_3_2.Y);
        inputWire(A(i - 1)).connectTo(subCircuits[unfixedGt(i)].inputWires, GT_3_1.X);
        inputWire(A(i - 1)).connectTo(subCircuits[2 * bitLength + 1].inputWires, MUX_2Lplus1_L.X(i));

        subCircuits[fixedAdder(i - 1)].outputWire(ADD_3_2.COUT)
            .connectTo(subCircuits[fixedAdder(i)].inputWires, ADD_3_2.CIN);
        subCircuits[unfixedGt(i - 1)].outputWire()
            .connectTo(subCircuits[unfixedGt(i)].inputWires, GT_3_1.C);
      }
    }
    inputWire(A(bitLength - 1)).connectTo(subCircuits[2 * bitLength].inputWires, LEFT_INPUT);
    subCircuits[unfixedGt(bitLength - 1)].outputWire()
        .connectTo(subCircuits[2 * bitLength].inputWires, RIGHT_INPUT);
    subCircuits[2 * bitLength].outputWire()
        .connectTo(subCircuits[2 * bitLength + 1].inputWires, MUX_2Lplus1_L.C(bitLength));
  }

  protected void defineOutputWires() {
    System.arraycopy(subCircuits[2 * bitLength + 1].outputWires, 0, outputWires, 0, bitLength);
  }

  protected void fixInternalWires() {
    subCircuits[fixedAdder(0)].inputWire(ADD_3_2.Y)
        .fixWire(0);
    subCircuits[fixedAdder(0)].inputWire(ADD_3_2.CIN)
        .fixWire(0);
    subCircuits[unfixedGt(0)].inputWire(GT_3_1.X)
        .fixWire(0);
    subCircuits[unfixedGt(0)].inputWire(GT_3_1.C)
        .fixWire(0);
    subCircuits[bitLength * 2 + 1].inputWire(MUX_2Lplus1_L.X(0))
        .fixWire(0);
  }

  private int fixedAdder(int i) {
    return 2 * i;
  }

  private int unfixedGt(int i) {
    return 2 * i + 1;
  }
}
