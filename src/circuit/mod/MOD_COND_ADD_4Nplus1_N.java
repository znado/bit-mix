// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package circuit.mod;

import circuit.CompositeCircuit;
import circuit.core.MUX_2Lplus1_L;
import circuit.core.bool.ADD_3_2;
import circuit.core.bool.AND_2_1;
import circuit.core.bool.GT_3_1;
import circuit.core.bool.OR_2_1;


/*
 * Modular addition circuit which conditionally calculates A+B mod M, where M-1 must have its top bit set to 1.
 * Accepts an additional bit addB, which (if not present) causes B to short-circuit to 0.
 *
 * Takes A (an n-bit integer less than M), B (an n-bit integer less than M),
 *  M-1 (where M is an n-bit integer which is at least 2^(n-1)+1), and invM (n bits, -M mod 2^n)
 */
public class MOD_COND_ADD_4Nplus1_N extends CompositeCircuit {
  private final int bitLength;

  public MOD_COND_ADD_4Nplus1_N(int n) {
    super(4 * n + 1, n, 4 * n + 2, "MOD_COND_ADD_" + (4 * n + 1) + "_" + n);
    bitLength = n;
  }

  public static int A(int i) {
    return 4 * i + 1;
  }

  public static int B(int i) {
    return 4 * i + 3;
  }

  public static int addB() {
    return 0;
  }

  public static int invM(int i) {
    return 4 * i + 2;
  }

  public static int mMinusOne(int i) {
    return 4 * i + 4;
  }

  private int OR() {
    return 4 * bitLength;
  }

  private int bit(int i) {
    return 4 * i;
  }

  protected void createSubCircuits(final boolean isForGarbling) {
    for (int i = 0; i < bitLength; i++) {
      subCircuits[bit(i)] = AND_2_1.newInstance(isForGarbling);
      subCircuits[unfixedAdder(i)] = new ADD_3_2();
      subCircuits[fixedAdder(i)] = new ADD_3_2();
      subCircuits[unfixedGt(i)] = new GT_3_1();
    }
    subCircuits[OR()] = OR_2_1.newInstance(isForGarbling);
    subCircuits[selector()] = new MUX_2Lplus1_L(bitLength);
  }

  protected void connectWires() {
    for (int i = 0; i < bitLength; i++) {
      inputWire(B(i)).connectTo(subCircuits[bit(i)].inputWires, LEFT_INPUT);
      inputWire(addB()).connectTo(subCircuits[bit(i)].inputWires, RIGHT_INPUT);
      inputWire(A(i)).connectTo(subCircuits[unfixedAdder(i)].inputWires, ADD_3_2.X);
      subCircuits[bit(i)].outputWire()
          .connectTo(subCircuits[unfixedAdder(i)].inputWires, ADD_3_2.Y);

      inputWire(invM(i)).connectTo(subCircuits[fixedAdder(i)].inputWires, ADD_3_2.X);
      subCircuits[unfixedAdder(i)].outputWire(ADD_3_2.S)
          .connectTo(subCircuits[fixedAdder(i)].inputWires, ADD_3_2.Y);

      inputWire(mMinusOne(i)).connectTo(subCircuits[unfixedGt(i)].inputWires, GT_3_1.Y);
      subCircuits[unfixedAdder(i)].outputWire(ADD_3_2.S)
          .connectTo(subCircuits[unfixedGt(i)].inputWires, GT_3_1.X);

      subCircuits[unfixedAdder(i)].outputWire(ADD_3_2.S)
          .connectTo(subCircuits[selector()].inputWires, MUX_2Lplus1_L.X(i));
      subCircuits[fixedAdder(i)].outputWire(ADD_3_2.S)
          .connectTo(subCircuits[selector()].inputWires, MUX_2Lplus1_L.Y(i));

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
        .connectTo(subCircuits[OR()].inputWires, LEFT_INPUT);
    subCircuits[unfixedGt(bitLength - 1)].outputWire()
        .connectTo(subCircuits[OR()].inputWires, RIGHT_INPUT);
    subCircuits[OR()].outputWire()
        .connectTo(subCircuits[selector()].inputWires, MUX_2Lplus1_L.C(bitLength));
  }

  protected void defineOutputWires() {
    System.arraycopy(subCircuits[selector()].outputWires, 0, outputWires, 0, bitLength);
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
    return 4 * i + 2;
  }

  private int selector() {
    return 4 * bitLength + 1;
  }

  private int unfixedAdder(int i) {
    return 4 * i + 1;
  }

  private int unfixedGt(int i) {
    return 4 * i + 3;
  }
}
