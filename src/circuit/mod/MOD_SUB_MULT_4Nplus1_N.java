package circuit.mod;

import circuit.CompositeCircuit;


/*
 * Takes in A (accumulator), X (multiplicand), invX (X-M mod 2^bitlength), M-1 (where M is an n-bit modulus
 * greater than 2^(n-1)+1), invM (-M mod 2^n), and y_i (the single bit of the multiplicator being addressed).
 * Outputs the next accumulator
 */
public class MOD_SUB_MULT_4Nplus1_N extends CompositeCircuit {
  private static final int COND_ADD = 1;
  private static final int DOUBLED_ACCUM = 0;
  private final int bitlength;

  public MOD_SUB_MULT_4Nplus1_N(int n) {
    super(4 * n + 1, n, 2, "MOD_SUB_MULT_" + (4 * n + 1) + "_" + n);
    bitlength = n;
  }

  public static int A(int i) {
    return 1 + 4 * i;
  }

  public static int X(int i) {
    return 2 + 4 * i;
  }

  public static int Y_i() {
    return 0;
  }

  public static int invM(int i) {
    return 4 + 4 * i;
  }

  public static int mMinusOne(int i) {
    return 3 + 4 * i;
  }

  protected void createSubCircuits(final boolean isForGarbling) {
    subCircuits[DOUBLED_ACCUM] = new MOD_DOUBLE_3N_N(bitlength);
    subCircuits[COND_ADD] = new MOD_COND_ADD_4Nplus1_N(bitlength);
  }

  protected void connectWires() {
    for (int i = 0; i < bitlength; i++) {
      inputWires[A(i)].connectTo(subCircuits[DOUBLED_ACCUM].inputWires, MOD_DOUBLE_3N_N.A(i));
      inputWires[mMinusOne(i)].connectTo(subCircuits[DOUBLED_ACCUM].inputWires, MOD_DOUBLE_3N_N.mMinusOne(i));
      inputWires[invM(i)].connectTo(subCircuits[DOUBLED_ACCUM].inputWires, MOD_DOUBLE_3N_N.invM(i));

      subCircuits[DOUBLED_ACCUM].outputWires[i].connectTo(subCircuits[COND_ADD].inputWires,
          MOD_COND_ADD_4Nplus1_N.A(i));
      inputWires[X(i)].connectTo(subCircuits[COND_ADD].inputWires, MOD_COND_ADD_4Nplus1_N.B(i));
      inputWires[invM(i)].connectTo(subCircuits[COND_ADD].inputWires, MOD_COND_ADD_4Nplus1_N.invM(i));
      inputWires[mMinusOne(i)].connectTo(subCircuits[COND_ADD].inputWires, MOD_COND_ADD_4Nplus1_N.mMinusOne(i));
    }
    inputWires[Y_i()].connectTo(subCircuits[COND_ADD].inputWires, MOD_COND_ADD_4Nplus1_N.addB());
  }

  protected void defineOutputWires() {
    System.arraycopy(subCircuits[COND_ADD].outputWires, 0, outputWires, 0, bitlength);
  }
}
