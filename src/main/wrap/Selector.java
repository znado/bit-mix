package main.wrap;

import circuit.CompositeCircuit;
import circuit.core.MUX_2Lplus1_L;
import circuit.mod.MOD_SQUARE_3N_N;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class Selector extends CompositeCircuit {
  private static final int SEL1 = 1;
  private static final int SEL2 = 2;
  private static final int XOR = 0;
  private final int bitLength;

  public Selector(int bitLength) {
    super(2 * bitLength + 2, 2 * bitLength, 3, "SELECTOR");
    this.bitLength = bitLength;
  }

  public static int X(int bitLength, int i) {
    return 2 + i;
  }

  public static int Y(int bitLength, int i) {
    return 2 + bitLength + i;
  }

  public static int bit(int i) {
    return i;
  }

  protected void createSubCircuits(final boolean isForGarbling) {
    subCircuits[XOR] = new MOD_SQUARE_3N_N(bitLength);
    subCircuits[SEL1] = new MUX_2Lplus1_L(bitLength);
    subCircuits[SEL2] = new MUX_2Lplus1_L(bitLength);
  }

  protected void connectWires() {
    for (int i = 0; i < bitLength; i++) {
      inputWire(X(bitLength, i)).connectTo(subCircuits[SEL1].inputWires, MUX_2Lplus1_L.X(i));
      inputWire(X(bitLength, i)).connectTo(subCircuits[SEL2].inputWires, MUX_2Lplus1_L.Y(i));

      inputWire(Y(bitLength, i)).connectTo(subCircuits[SEL1].inputWires, MUX_2Lplus1_L.Y(i));
      inputWire(Y(bitLength, i)).connectTo(subCircuits[SEL2].inputWires, MUX_2Lplus1_L.X(i));
    }
    inputWire(bit(0)).connectTo(subCircuits[XOR].inputWires, LEFT_INPUT);
    inputWire(bit(1)).connectTo(subCircuits[XOR].inputWires, RIGHT_INPUT);
    subCircuits[XOR].outputWire()
        .connectTo(subCircuits[SEL1].inputWires, MUX_2Lplus1_L.C(bitLength));
    subCircuits[XOR].outputWire()
        .connectTo(subCircuits[SEL2].inputWires, MUX_2Lplus1_L.C(bitLength));
  }

  @Override
  protected void defineOutputWires() {
    System.arraycopy(subCircuits[SEL1].outputWires, 0, outputWires, 0, bitLength);
    System.arraycopy(subCircuits[SEL2].outputWires, 0, outputWires, bitLength, bitLength);
  }
}
