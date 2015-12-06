// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main.wrap;

import circuit.CompositeCircuit;
import circuit.core.MUX_2Lplus1_L;
import circuit.mod.MOD_MULT_4N_N;
import circuit.mod.MOD_SQUARE_3N_N;
import main.AdditionCommon;


/*
 * Modular multiplication circuit that calculates XY mod M. Requires that X and Y are both currently less than M,
 *  and additionally that the highest bit of M-1 is 1 (so M >= 2^(n-1)+1). Accepts X (the n-bit multiplicand),
 *  Y (the n-bit multiplier), M-1 (the n-bit modulus), -M (aka invM, 2^n-M), and X-M (aka invX, 2^n-M+X).
 */
public class EXP_STEP extends CompositeCircuit {
  private final boolean first;

  public EXP_STEP(boolean first) {
    super(first ? 1 : AdditionCommon.BIT_LENGTH + 1, AdditionCommon.BIT_LENGTH, 3, "EXP STEP");
    this.first = first;
  }

  private static final int SQUARE = 0;
  private static final int MULT = 1;
  private static final int SEL = 2;

  public static int X(int i) {
    return i + 1;
  }

  public static int Y() {
    return 0;
  }

  protected void createSubCircuits() throws Exception {
    subCircuits[SQUARE] = new MOD_SQUARE_3N_N(AdditionCommon.BIT_LENGTH);
    subCircuits[MULT] = new MOD_MULT_4N_N(AdditionCommon.BIT_LENGTH);
    subCircuits[SEL] = new MUX_2Lplus1_L(AdditionCommon.BIT_LENGTH);

    super.createSubCircuits();
  }

  protected void connectWires() {
    for (int i = 0; i < AdditionCommon.BIT_LENGTH; i++) {
      if (!first) {
        inputWire(X(i)).connectTo(subCircuits[SQUARE].inputWires, MOD_SQUARE_3N_N.X(i));
      }
      subCircuits[SQUARE].outputWire(i).connectTo(subCircuits[MULT].inputWires, MOD_MULT_4N_N.X(i));
      subCircuits[SQUARE].outputWire(i).connectTo(subCircuits[SEL].inputWires, MUX_2Lplus1_L.X(i));
      subCircuits[MULT].outputWire(i).connectTo(subCircuits[SEL].inputWires, MUX_2Lplus1_L.Y(i));
    }
    inputWire(Y()).connectTo(subCircuits[SEL].inputWires, MUX_2Lplus1_L.C(AdditionCommon.BIT_LENGTH));
  }

  @Override
  protected void defineOutputWires() {
    System.arraycopy(subCircuits[SEL].outputWires, 0, outputWires, 0, AdditionCommon.BIT_LENGTH);
  }

  @Override
  protected void fixInternalWires() {
    for (int i = 0; i < AdditionCommon.BIT_LENGTH; i++) {
      subCircuits[SQUARE].inputWire(MOD_SQUARE_3N_N.mMinusOne(i))
          .fixWire(AdditionCommon.MOD_MINUS_ONE.testBit(i) ? 1 : 0);
      subCircuits[MULT].inputWire(MOD_MULT_4N_N.mMinusOne(i))
          .fixWire(AdditionCommon.MOD_MINUS_ONE.testBit(i) ? 1 : 0);

      subCircuits[SQUARE].inputWire(MOD_SQUARE_3N_N.invM(i))
          .fixWire(AdditionCommon.MOD_NEG.testBit(i) ? 1 : 0);
      subCircuits[MULT].inputWire(MOD_MULT_4N_N.invM(i))
          .fixWire(AdditionCommon.MOD_NEG.testBit(i) ? 1 : 0);

      subCircuits[MULT].inputWire(MOD_MULT_4N_N.Y(i))
          .fixWire(AdditionCommon.BASE.testBit(i) ? 1 : 0);

      if (first) {
        subCircuits[SQUARE].inputWire(MOD_SQUARE_3N_N.X(i)).fixWire(i == 0 ? 1 : 0);
      }
    }
  }
}
