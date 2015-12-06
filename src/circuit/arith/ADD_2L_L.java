// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package circuit.arith;

import circuit.CompositeCircuit;
import circuit.Wire;
import circuit.core.bool.ADD_3_2;


/**
 * Addition circuit with ignored overflow
 */
public class ADD_2L_L extends CompositeCircuit {
  private final int L;

  public ADD_2L_L(int l) {
    super(2 * l, l, l, "ADD_" + (2 * l) + "_" + l);

    L = l;
  }

  protected void createSubCircuits() throws Exception {
    for (int i = 0; i < L; i++) {
      subCircuits[i] = new ADD_3_2();
    }

    super.createSubCircuits();
  }

  protected void connectWires() {
    inputWires[X(0)].connectTo(subCircuits[0].inputWires, ADD_3_2.X);
    inputWires[Y(0)].connectTo(subCircuits[0].inputWires, ADD_3_2.Y);

    for (int i = 1; i < L; i++) {
      inputWires[X(i)].connectTo(subCircuits[i].inputWires, ADD_3_2.X);
      inputWires[Y(i)].connectTo(subCircuits[i].inputWires, ADD_3_2.Y);
      subCircuits[i - 1].outputWires[ADD_3_2.COUT].connectTo(subCircuits[i].inputWires, ADD_3_2.CIN);
    }
  }

  protected void defineOutputWires() {
    for (int i = 0; i < L; i++) {
      outputWires[i] = subCircuits[i].outputWires[ADD_3_2.S];
    }
  }

  protected void fixInternalWires() {
    Wire internalWire = subCircuits[0].inputWires[ADD_3_2.CIN];
    internalWire.fixWire(0);
  }

  public static int X(int i) {
    return 2*i;
  }

  public static int Y(int i) {
    return 2*i + 1;
  }
}