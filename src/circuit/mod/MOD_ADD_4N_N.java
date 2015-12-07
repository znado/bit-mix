// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package circuit.mod;

import circuit.CompositeCircuit;
import circuit.core.MUX_2Lplus1_L;
import circuit.core.bool.ADD_3_2;
import circuit.core.bool.GT_3_1;
import circuit.core.bool.OR_2_1;


/**
 * Modular addition circuit which calculates A+B mod M, where M-1 must have its top bit set to 1. Takes A (an n-bit
 * integer less than M), B (an n-bit integer less than M), M-1 (where M is an n-bit integer which is at least
 * 2^(n-1)+1), and invM (n bits, -M mod 2^n)
 */
public class MOD_ADD_4N_N extends CompositeCircuit {
  private final int bitLength;

  public MOD_ADD_4N_N(int n) {
    super(4 * n, n, 3 * n + 2, "MOD_ADD'_" + (4 * n) + "_" + n);
    bitLength = n;
  }

  public static int A(int i) {
    return 4 * i;
  }

  public static int B(int i) {
    return 4 * i + 2;
  }

  public static int invM(int i) {
    return 4 * i + 1;
  }

  public static int mMinusOne(int i) {
    return 4 * i + 3;
  }

  protected void createSubCircuits(final boolean isForGarbling) {
    for (int i = 0; i < bitLength; i++) {
      subCircuits[unfixedAdder(i)] = new ADD_3_2();
      subCircuits[fixedAdder(i)] = new ADD_3_2();
      subCircuits[unfixedGt(i)] = new GT_3_1();
    }
    subCircuits[3 * bitLength] = OR_2_1.newInstance(isForGarbling);
    subCircuits[3 * bitLength + 1] = new MUX_2Lplus1_L(bitLength);
  }

  protected void connectWires() {
    for (int i = 0; i < bitLength; i++) {
      inputWire(A(i)).connectTo(subCircuits[unfixedAdder(i)].inputWires, ADD_3_2.X);
      inputWire(B(i)).connectTo(subCircuits[unfixedAdder(i)].inputWires, ADD_3_2.Y);

      inputWire(invM(i)).connectTo(subCircuits[fixedAdder(i)].inputWires, ADD_3_2.X);
      subCircuits[unfixedAdder(i)].outputWire(ADD_3_2.S)
          .connectTo(subCircuits[fixedAdder(i)].inputWires, ADD_3_2.Y);

      inputWire(mMinusOne(i)).connectTo(subCircuits[unfixedGt(i)].inputWires, GT_3_1.Y);
      subCircuits[unfixedAdder(i)].outputWire(ADD_3_2.S)
          .connectTo(subCircuits[unfixedGt(i)].inputWires, GT_3_1.X);

      subCircuits[unfixedAdder(i)].outputWire(ADD_3_2.S)
          .connectTo(subCircuits[3 * bitLength + 1].inputWires, MUX_2Lplus1_L.X(i));
      subCircuits[fixedAdder(i)].outputWire(ADD_3_2.S)
          .connectTo(subCircuits[3 * bitLength + 1].inputWires, MUX_2Lplus1_L.Y(i));

      if (i != 0) {
        subCircuits[unfixedAdder(i - 1)].outputWire(ADD_3_2.COUT)
            .connectTo(subCircuits[unfixedAdder(i)].inputWires, ADD_3_2.CIN);
        subCircuits[fixedAdder(i - 1)].outputWire(ADD_3_2.COUT)
            .connectTo(subCircuits[fixedAdder(i)].inputWires, ADD_3_2.CIN);
        subCircuits[unfixedGt(i - 1)].outputWire()
            .connectTo(subCircuits[unfixedGt(i)].inputWires, GT_3_1.C);
      }
    }
    subCircuits[unfixedAdder(bitLength - 1)].outputWire(ADD_3_2.COUT)
        .connectTo(subCircuits[3 * bitLength].inputWires, LEFT_INPUT);
    subCircuits[unfixedGt(bitLength - 1)].outputWire()
        .connectTo(subCircuits[3 * bitLength].inputWires, RIGHT_INPUT);
    subCircuits[3 * bitLength].outputWire()
        .connectTo(subCircuits[3 * bitLength + 1].inputWires, MUX_2Lplus1_L.C(bitLength));
  }

  protected void defineOutputWires() {
    System.arraycopy(subCircuits[3 * bitLength + 1].outputWires, 0, outputWires, 0, bitLength);
  }

  protected void fixInternalWires() {
    subCircuits[unfixedAdder(0)].inputWire(ADD_3_2.CIN)
        .fixWire(0);
    subCircuits[fixedAdder(0)].inputWire(ADD_3_2.CIN)
        .fixWire(0);
    subCircuits[unfixedGt(0)].inputWire(GT_3_1.C)
        .fixWire(0);
  }

  private int fixedAdder(int i) {
    return 3 * i + 1;
  }

  private int unfixedAdder(int i) {
    return 3 * i;
  }

  private int unfixedGt(int i) {
    return 3 * i + 2;
  }
}
