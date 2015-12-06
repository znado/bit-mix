package main.wrap;

import main.AdditionCommon;
import circuit.CompositeCircuit;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public abstract class Wrapper extends CompositeCircuit {

  protected Wrapper() {
    this(AdditionCommon.BIT_LENGTH);
  }

  protected Wrapper(int outputSize) {
    super(2 * AdditionCommon.BIT_LENGTH, outputSize, 1, "Wrapper");
    setRunFlag();
  }

  public abstract void setRunFlag();

  protected void defineOutputWires() {
    System.arraycopy(subCircuits[subCircuits.length - 1].outputWires, 0, outputWires, 0, outputWires.length);
  }

  protected final void fixInternalWires() {
    fixConstants();
  }

  public void fixConstants() {}
}
