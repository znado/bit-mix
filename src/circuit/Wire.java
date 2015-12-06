// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package circuit;

import java.math.BigInteger;
import java.security.SecureRandom;


public class Wire extends TransitiveObservable {
  public static final int UNKNOWN_SIG = -1;

  // These four fields are for garbling
  public static int K = 0;
  private static SecureRandom rnd = new SecureRandom();
  public static final int labelBitLength = 80;

  public static final BigInteger R = new BigInteger(labelBitLength - 1, rnd);

  public final int serialNum;
  public int value = UNKNOWN_SIG;
  public BigInteger lbl;
  public boolean invd = false;

  public Wire() {
    serialNum = K++;
    lbl = new BigInteger(labelBitLength, rnd);
  }

  public static BigInteger[] newLabelPair() {
    BigInteger[] res = new BigInteger[2];
    res[0] = new BigInteger(labelBitLength, rnd);
    res[1] = conjugate(res[0]);
    return res;
  }

  public static BigInteger conjugate(BigInteger label) {
    if (label == null) {
      return null;
    }

    return label.xor(R.shiftLeft(1)
        .setBit(0));
  }

  public void setLabel(BigInteger label) {
    lbl = label;
  }

  public void setReady() {
    setChanged();
    notifyObservers();
  }



  public void connectTo(Wire[] ws, int idx) {
    Wire w = ws[idx];

    for (int i = 0; i < w.observers.size(); i++) {
      TransitiveObserver ob = w.observers.get(i);
      TransitiveObservable.Socket s = w.exports.get(i);
      this.addObserver(ob, s);
      s.updateSocket(this);
    }

    w.deleteObservers();
    ws[idx] = this;
  }

  public void fixWire(int v) {
    this.value = v;

    for (TransitiveObserver observer : this.observers) {
      Circuit c = (Circuit) observer;
      c.inDegree--;
      if (c.inDegree == 0) {
        c.compute();
        for (int j = 0; j < c.outDegree; j++) {
          c.outputWires[j].fixWire(c.outputWires[j].value);
        }
      }
    }
  }
}
