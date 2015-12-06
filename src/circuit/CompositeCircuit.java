// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package circuit;

import java.util.HashMap;
import java.util.Map;


public abstract class CompositeCircuit extends Circuit {
  protected Circuit[] subCircuits;
  protected int nSubCircuits;

  public CompositeCircuit(int inDegree, int outDegree, int nSubCircuits, String name) {
    super(inDegree, outDegree, name);

    this.nSubCircuits = nSubCircuits;

    subCircuits = new Circuit[nSubCircuits];
  }

  public void build() throws Exception {
    createInputWires();
    createSubCircuits();
    connectWires();
    defineOutputWires();
    fixInternalWires();
    if (!checkInputsOutputs()) {
      throw new IllegalStateException("Unsafe wire assignment");
    }
  }

  private boolean checkInputsOutputs() {
    Map<Wire, Integer> inputs = new HashMap<>();
    for (int i = 0; i < inputWires.length; i++) {
      Integer existed = inputs.put(inputWires[i], i);
      if (existed != null) {
        System.err.printf("Unsafe wire assignment: two input wires (%d,%d) are the same%n", existed, i);
        return false;
      }
    }
    for (int i = 0; i < outputWires.length; i++) {
      if (inputs.containsKey(outputWires[i])) {
        System.err.printf("Unsafe wire assignment: input and output wires are equal (%d->%d)%n",
            inputs.get(outputWires[i]), i);
        return false;
      }
    }
    return true;
  }

  protected void createSubCircuits() throws Exception {
    for (int i = 0; i < nSubCircuits; i++) {
      subCircuits[i].build();
    }
  }

  abstract protected void connectWires() throws Exception;
  abstract protected void defineOutputWires();

  protected void fixInternalWires() {}

  protected void compute() {}

  protected void execute() {}
}
