// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package crypto.ot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;


public abstract class Sender {
  protected int numOfPairs;
  protected int msgBitLength;
  protected BigInteger[][] msgPairs;

  protected ObjectInputStream ois;
  protected ObjectOutputStream oos;

  public Sender(int numOfPairs, int msgBitLength, ObjectInputStream in, ObjectOutputStream out) {
    this.numOfPairs = numOfPairs;
    this.msgBitLength = msgBitLength;
    ois = in;
    oos = out;
  }

  public void execProtocol(BigInteger[][] msgPairs) throws IOException {
    if (msgPairs.length != numOfPairs) {
      throw new IllegalArgumentException("Message pair length error: " +
          msgPairs.length + " != " + numOfPairs);
    }

    this.msgPairs = msgPairs;
  }
}