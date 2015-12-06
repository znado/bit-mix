// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main.wrap;

import circuit.mod.MOD_SQUARE_3N_N;
import main.AdditionCommon;


/*
 * Modular multiplication circuit that calculates XY mod M. Requires that X and Y are both currently less than M,
 *  and additionally that the highest bit of M-1 is 1 (so M >= 2^(n-1)+1). Accepts X (the n-bit multiplicand),
 *  Y (the n-bit multiplier), M-1 (the n-bit modulus), -M (aka invM, 2^n-M), and X-M (aka invX, 2^n-M+X).
 */
public class RUN_SQUARE extends Wrapper {
  public static boolean wasRun = false;

  protected void createSubCircuits() throws Exception {
    subCircuits[0] = new MOD_SQUARE_3N_N(AdditionCommon.BIT_LENGTH);
    super.createSubCircuits();
  }

  protected void connectWires() {
    for (int i = 0; i < AdditionCommon.BIT_LENGTH; i++) {
      inputWire(Wrappers.X(i)).connectTo(subCircuits[0].inputWires, MOD_SQUARE_3N_N.X(i));
    }
  }

  public void setRunFlag() {
    wasRun = true;
  }

  public void fixConstants() {
    for (int i = 0; i < AdditionCommon.BIT_LENGTH; i++) {
      subCircuits[0].inputWire(MOD_SQUARE_3N_N.mMinusOne(i))
          .fixWire(AdditionCommon.MOD_MINUS_ONE.testBit(i) ? 1 : 0);
      subCircuits[0].inputWire(MOD_SQUARE_3N_N.invM(i))
          .fixWire(AdditionCommon.MOD_NEG.testBit(i) ? 1 : 0);
    }
  }
}
