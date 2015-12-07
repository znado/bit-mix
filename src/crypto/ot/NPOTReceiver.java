// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package crypto.ot;

import crypto.Cipher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;


public class NPOTReceiver extends Receiver {
  private static SecureRandom rnd = new SecureRandom();

  private int msgBitLength;
  private BigInteger p, q, g, C;
  private BigInteger gr;

  private BigInteger[] gk, C_over_gk;
  private BigInteger[][] pk;

  private BigInteger[] keys;

  public NPOTReceiver(int numOfChoices, ObjectInputStream in, ObjectOutputStream out) throws IOException {
    super(numOfChoices, in, out);

    initialize();
  }

  public void execProtocol(BigInteger choices) throws IOException {
    super.execProtocol(choices);

    step1();
    step2();
  }

  private void initialize() throws IOException {
    try {
      C = (BigInteger) ois.readObject();
      p = (BigInteger) ois.readObject();
      q = (BigInteger) ois.readObject();
      g = (BigInteger) ois.readObject();
      gr = (BigInteger) ois.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException("Received an unknown class type", e);
    }
    msgBitLength = ois.readInt();

    gk = new BigInteger[numOfChoices];
    C_over_gk = new BigInteger[numOfChoices];
    keys = new BigInteger[numOfChoices];
    for (int i = 0; i < numOfChoices; i++) {
      BigInteger k = (new BigInteger(q.bitLength(), rnd)).mod(q);
      gk[i] = g.modPow(k, p);
      C_over_gk[i] = C.multiply(gk[i].modInverse(p))
          .mod(p);
      keys[i] = gr.modPow(k, p);
    }
  }

  private void step1() throws IOException {
    pk = new BigInteger[numOfChoices][2];
    BigInteger[] pk0 = new BigInteger[numOfChoices];
    for (int i = 0; i < numOfChoices; i++) {
      int sigma = choices.testBit(i) ? 1 : 0;
      pk[i][sigma] = gk[i];
      pk[i][1 - sigma] = C_over_gk[i];

      pk0[i] = pk[i][0];
    }

    oos.writeObject(pk0);
    oos.flush();
  }

  private void step2() throws IOException {
    BigInteger[][] msg;
    try {
      msg = (BigInteger[][]) ois.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException("Received class of unknown type", e);
    }

    data = new BigInteger[numOfChoices];
    for (int i = 0; i < numOfChoices; i++) {
      int sigma = choices.testBit(i) ? 1 : 0;
      data[i] = Cipher.decrypt(keys[i], msg[i][sigma], msgBitLength);
    }
  }
}
