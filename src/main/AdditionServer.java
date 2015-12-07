// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main;

import util.StopWatch;
import util.Utils;
import circuit.State;
import circuit.Wire;

import java.math.BigInteger;
import java.security.SecureRandom;


public class AdditionServer extends ProgServer {
  private BigInteger x;
  private BigInteger xInv;
  private BigInteger output;

  private State outputState;

  private BigInteger[][] sBitslps, cBitslps;

  private static final SecureRandom rnd = new SecureRandom();

  public AdditionServer(BigInteger x) {
    this.x = x;

    this.xInv = this.x.add(AdditionCommon.MOD_NEG);
    AdditionCommon.display("x", x);
    AdditionCommon.display("y", AdditionCommon.Y);
    AdditionCommon.display("m", AdditionCommon.MODULUS);
    AdditionCommon.display("2^n - m", AdditionCommon.MOD_NEG);
    AdditionCommon.display("x-m+2^n", xInv);
    AdditionCommon.display("g", AdditionCommon.BASE);
  }

  protected void init() throws Exception {
    AdditionCommon.oos.writeInt(AdditionCommon.BIT_LENGTH);
    AdditionCommon.oos.flush();

    StopWatch.start();

    AdditionCommon.initCircuits();

    generateLabelPairs();

    super.init();
  }

  private static void generatePair(BigInteger[] pair) {
    if (pair.length != 2) {
      throw new IllegalArgumentException("Cannot generate a pair of size " + pair.length);
    }
    BigInteger glb0 = new BigInteger(Wire.labelBitLength, rnd);
    BigInteger glb1 = glb0.xor(Wire.R.shiftLeft(1)
        .setBit(0));
    pair[0] = glb0;
    pair[1] = glb1;
  }

  private void generateLabelPairs() {
    sBitslps = new BigInteger[AdditionCommon.BIT_LENGTH][2];
    cBitslps = new BigInteger[AdditionCommon.BIT_LENGTH][2];

    for (int i = 0; i < AdditionCommon.BIT_LENGTH; i++) {
      generatePair(cBitslps[i]);
    }
    for (int i = 0; i < AdditionCommon.BIT_LENGTH; i++) {
      generatePair(sBitslps[i]);
    }
  }

  private void send(BigInteger b, int from) throws Exception {
    for (int i = 0; i < AdditionCommon.BIT_LENGTH; i++) {
      int idx = b.testBit(i) ? 1 : 0;

      int bytelength = (Wire.labelBitLength - 1) / 8 + 1;
      Utils.writeBigInteger(sBitslps[from+i][idx], bytelength, AdditionCommon.oos);
    }
  }

  protected void execTransfer() throws Exception {
    send(x, 0);

    AdditionCommon.oos.flush();

    snder.execProtocol(cBitslps);
  }

  protected void execCircuit() throws Exception {
    BigInteger[] sBitslbs = new BigInteger[AdditionCommon.BIT_LENGTH];
    BigInteger[] cBitslbs = new BigInteger[AdditionCommon.BIT_LENGTH];

    for (int i = 0; i < sBitslps.length; i++) {
      sBitslbs[i] = sBitslps[i][0];
    }

    for (int i = 0; i < cBitslps.length; i++) {
      cBitslbs[i] = cBitslps[i][0];
    }

    outputState = AdditionCommon.execCircuit(sBitslbs, cBitslbs);
  }

  protected void interpretResult() throws Exception {
    BigInteger[] outLabels = (BigInteger[]) AdditionCommon.ois.readObject();

    output = BigInteger.ZERO;
    for (int i = 0; i < outLabels.length; i++) {
      if (outputState.wires[i].value != Wire.UNKNOWN_SIG) {
        if (outputState.wires[i].value == 1) {
          output = output.setBit(i);
        }
      } else if (outLabels[i].equals(outputState.wires[i].invd ? outputState.wires[i].lbl
          : outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1)
              .setBit(0)))) {
        output = output.setBit(i);
      } else if (!outLabels[i].equals(outputState.wires[i].invd ? outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1)
          .setBit(0)) : outputState.wires[i].lbl)) {
        System.err.println("Output so far: " + output.toString(2) + " (" + (i-1) + " of " + outLabels.length + " bits)");
        throw new Exception("Bad label encountered: i = " + i + "\t" +
            outLabels[i] + " != (" +
            outputState.wires[i].lbl + ", " +
            outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1)
                .setBit(0)) + ")");
      }
    }
    StopWatch.stop();
    System.out.println("Time for run (ms): " + StopWatch.getElapsedTime());
  }

  protected void verify_result() throws Exception {
    BigInteger y = (BigInteger) AdditionCommon.ois.readObject();

    //BigInteger res = Wrappers.result(x, y);
    BigInteger res = AdditionCommon.BASE.modPow(x.multiply(y).mod(AdditionCommon.MODULUS), AdditionCommon.MODULUS);

    System.out.println("\n\nVERIFY");
    //AdditionCommon.display(Wrappers.resultString(), res);
    AdditionCommon.display("g^xy mod m", res);
    AdditionCommon.display("output", output);
    AdditionCommon.display("diff", res.xor(output));
  }
}