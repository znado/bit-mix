// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main;

import util.StopWatch;
import util.Utils;
import circuit.State;
import circuit.Wire;

import java.math.BigInteger;


public class AdditionClient extends ProgClient {
  private BigInteger cBits;
  private BigInteger[] sBitslbs, cBitslbs;

  private State outputState;

  public AdditionClient(BigInteger bv) {
    cBits = bv;
  }

  protected void init() throws Exception {
    if (AdditionCommon.BIT_LENGTH != AdditionCommon.ois.readInt()) {
      throw new IllegalStateException("Different bit lengths!");
    }

    AdditionCommon.initCircuits();

    otNumOfPairs = AdditionCommon.BIT_LENGTH;

    super.init();
  }

  protected void execTransfer() throws Exception {
    sBitslbs = new BigInteger[AdditionCommon.BIT_LENGTH];

    for (int i = 0; i < AdditionCommon.BIT_LENGTH; i++) {
      int bytelength = (Wire.labelBitLength - 1) / 8 + 1;
      sBitslbs[i] = Utils.readBigInteger(bytelength, AdditionCommon.ois);
    }
    StopWatch.taskTimeStamp("receiving labels for peer's inputs");

    cBitslbs = new BigInteger[AdditionCommon.BIT_LENGTH];
    rcver.execProtocol(cBits);
    cBitslbs = rcver.getData();
    StopWatch.taskTimeStamp("receiving labels for self's inputs");
  }

  protected void execCircuit() throws Exception {
    outputState = AdditionCommon.execCircuit(sBitslbs, cBitslbs);
  }

  protected void interpretResult() throws Exception {
    AdditionCommon.oos.writeObject(outputState.toLabels());
    AdditionCommon.oos.flush();
  }

  protected void verify_result() throws Exception {
    AdditionCommon.oos.writeObject(cBits);
    AdditionCommon.oos.flush();
  }
}