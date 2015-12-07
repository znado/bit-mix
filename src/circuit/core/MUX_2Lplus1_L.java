// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package circuit.core;

import circuit.CompositeCircuit;
import circuit.core.bool.MUX_3_1;


/*
 * Fig. 9 of [KSS09]
 * if C == 0, choose X; otherwise choose Y.
 */
public class MUX_2Lplus1_L extends CompositeCircuit {
  final int C;
  private final int L;

  public MUX_2Lplus1_L(int l) {
    super(2 * l + 1, l, l, "MUX_" + (2 * l + 1) + "_" + (2 * l));
    L = l;
    C = 2 * L;
  }

  public static int C(int bitlength) {
    return 2 * bitlength;
  }

  public static int X(int i) {
    return 2 * i + 1;
  }

  public static int Y(int i) {
    return 2 * i;
  }

  protected void createSubCircuits(final boolean isForGarbling) {
    for (int i = 0; i < L; i++) {
      subCircuits[i] = new MUX_3_1();
    }
  }

  protected void connectWires() {
    for (int i = 0; i < L; i++) {
      inputWires[X(i)].connectTo(subCircuits[i].inputWires, MUX_3_1.X);
      inputWires[Y(i)].connectTo(subCircuits[i].inputWires, MUX_3_1.Y);
      inputWires[C].connectTo(subCircuits[i].inputWires, MUX_3_1.C);
    }
  }

  protected void defineOutputWires() {
    for (int i = 0; i < L; i++) {
      outputWires[i] = subCircuits[i].outputWires[0];
    }
  }
}
