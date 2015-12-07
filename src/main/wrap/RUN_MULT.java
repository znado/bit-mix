// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main.wrap;

import circuit.CompositeCircuit;
import circuit.mod.MOD_MULT_4N_N;
import circuit.mod.MOD_SQUARE_3N_N;
import util.BigIntegers;

import java.math.BigInteger;


/*
 * Modular multiplication circuit that calculates XY mod M. Requires that X and Y are both currently less than M,
 *  and additionally that the highest bit of M-1 is 1 (so M >= 2^(n-1)+1). Accepts X (the n-bit multiplicand),
 *  Y (the n-bit multiplier), M-1 (the n-bit modulus), -M (aka invM, 2^n-M), and X-M (aka invX, 2^n-M+X).
 */
public class RUN_MULT extends CompositeCircuit {
  private final int bitLength;
  private final BigInteger modulus;

  public RUN_MULT(int bitLength, BigInteger modulus) {
    super(2*bitLength, bitLength, 1, "RUN MULT");
    this.bitLength = bitLength;
    this.modulus = modulus;
  }

  public static int X(int i) {
    return 2*i;
  }

  public static int Y(int i) {
    return 2*i+1;
  }

  protected void createSubCircuits(final boolean isForGarbling) {
    subCircuits[0] = new MOD_MULT_4N_N(bitLength);
  }

  protected void connectWires() {
    for (int i = 0; i < bitLength; i++) {
      inputWire(X(i)).connectTo(subCircuits[0].inputWires, MOD_MULT_4N_N.X(i));
      inputWire(Y(i)).connectTo(subCircuits[0].inputWires, MOD_MULT_4N_N.Y(i));
    }
  }

  @Override
  protected void defineOutputWires() {
    System.arraycopy(subCircuits[0].outputWires, 0, outputWires, 0, bitLength);
  }

  @Override
  protected void fixInternalWires() {
    BigInteger modMinusOne = modulus.subtract(BigInteger.ONE);
    BigInteger modNeg = BigIntegers.negative(modulus);

    for (int i = 0; i < bitLength; i++) {
      subCircuits[0].inputWire(MOD_MULT_4N_N.mMinusOne(i))
          .fixWire(modMinusOne.testBit(i) ? 1 : 0);

      subCircuits[0].inputWire(MOD_MULT_4N_N.invM(i))
          .fixWire(modNeg.testBit(i) ? 1 : 0);

    }
  }
}
