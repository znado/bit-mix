// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package circuit;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;


public abstract class Circuit implements TransitiveObserver {
  public static final int LEFT_INPUT = 0;
  public static final int RIGHT_INPUT = 1;

  public static boolean isForGarbling;

  public Wire[] inputWires;
  public Wire[] outputWires;

  protected int inDegree, outDegree;
  protected String name;

  public String toString() {
    return name;
  }

  public static ObjectOutputStream oos = null;
  public static ObjectInputStream ois = null;

  private int inputWireCount = 0;

  public Circuit(int inDegree, int outDegree, String name) {
    if (inDegree <= 0) {
      throw new IllegalArgumentException("In degree must be positive, but was " + inDegree);
    }
    if (outDegree <= 0) {
      throw new IllegalArgumentException("Out degree must be positive, but was " + outDegree);
    }

    this.inDegree = inDegree;
    this.outDegree = outDegree;
    this.name = name;

    inputWires = new Wire[inDegree];
    outputWires = new Wire[outDegree];
  }

  public static void setIOStream(ObjectInputStream ois, ObjectOutputStream oos) {
    Circuit.ois = ois;
    Circuit.oos = oos;
  }

  public abstract void build() throws Exception;

  public Wire inputWire(int i) {
    if (i < 0 || i >= this.inDegree) {
      throw new IndexOutOfBoundsException("No such output wire: " + i);
    }
    return inputWires[i];
  }

  public Wire outputWire() {
    if (this.outDegree != 1) {
      throw new IllegalStateException("Should only call outputWire() if there is only one output, but there are " +
          this.outDegree);
    }
    return outputWires[0];
  }

  public Wire outputWire(int i) {
    if (i < 0 || i >= this.outDegree) {
      throw new IndexOutOfBoundsException("No such output wire: " + i);
    }
    return outputWires[i];
  }

  protected void createInputWires() {
    for (int i = 0; i < inDegree; i++) {
      inputWires[i] = new Wire();
    }
  }

  public void startExecuting(int[] vals, boolean[] invd, BigInteger[] glbs) throws Exception {
    if (vals.length != invd.length ||
        invd.length != glbs.length ||
        glbs.length != this.inDegree) {
      throw new Exception("Unmatched number of input labels.");
    }

    for (int i = 0; i < this.inDegree; i++) {
      inputWires[i].value = vals[i];
      inputWires[i].invd = invd[i];
      inputWires[i].setLabel(glbs[i]);
      inputWires[i].setReady();
    }
  }

  public State startExecuting(State s) {
    if (s.getWidth() != this.inDegree) {
      throw new IllegalArgumentException("Unmatched number of input labels: state had with " +
          s.getWidth() + ", but circuit has in degree " + inDegree);
    }

    for (int i = 0; i < this.inDegree; i++) {
      inputWires[i].value = s.wires[i].value;
      inputWires[i].invd = s.wires[i].invd;
      inputWires[i].setLabel(s.wires[i].lbl);
      inputWires[i].setReady();
    }

    return State.fromWires(this.outputWires);
  }

  public BigInteger interpretOutputELabels(BigInteger[] eLabels) {
    if (eLabels.length != outDegree) {
      throw new IllegalArgumentException("Length Error.");
    }

    BigInteger output = BigInteger.ZERO;
    for (int i = 0; i < this.outDegree; i++) {
      if (outputWires[i].value != Wire.UNKNOWN_SIG) {
        if (outputWires[i].value == 1) {
          output = output.setBit(i);
        }
      } else if (eLabels[i].equals(outputWires[i].invd ? outputWires[i].lbl : outputWires[i].lbl.xor(Wire.R.shiftLeft(1)
          .setBit(0)))) {
        output = output.setBit(i);
      } else if (!eLabels[i].equals(outputWires[i].invd ? outputWires[i].lbl.xor(Wire.R.shiftLeft(1)
          .setBit(0)) : outputWires[i].lbl)) {
        throw new IllegalStateException("Bad Label encountered at ouputWire[" + i + "]:\n" +
            eLabels[i] + " is neither " +
            outputWires[i].lbl + " nor " +
            outputWires[i].lbl.xor(Wire.R.shiftLeft(1)
                .setBit(0)));
      }
    }

    return output;
  }

  public void update(TransitiveObservable o, Object arg) {
    // What the shit are these arguments?
    inputWireCount++;
    if (inDegree == 0) {
      System.err.println(this);
    }
    if (inputWireCount % inDegree == 0) {
      execute();
    }
  }

  public int getInDegree() {
    return inDegree;
  }

  abstract protected void compute();
  abstract protected void execute();
}
