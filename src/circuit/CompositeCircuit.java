// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package circuit;

import main.Connection;

import java.io.IOException;
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

  @Override
  public void build(Connection connection) throws IOException {
    createInputWires();
    createSubCircuits(connection.isForGarbling());
    buildSubCircuits(connection);
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

  protected void buildSubCircuits(Connection connection) throws IOException {
    for (int i = 0; i < nSubCircuits; i++) {
      subCircuits[i].build(connection);
    }
  }

  protected abstract void createSubCircuits(boolean isForGarbling);
  protected abstract void connectWires();
  protected abstract void defineOutputWires();

  protected void fixInternalWires() {}

  protected void compute() {}

  protected void execute() {}
}
