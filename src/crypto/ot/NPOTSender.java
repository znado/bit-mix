// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package crypto.ot;

import crypto.Cipher;
import main.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;


public class NPOTSender extends Sender {

  private static SecureRandom rnd = new SecureRandom();
  private static final int certainty = 80;

  private final static int qLength = 512; //512;
  private final static int pLength = 15360; //15360;

  private BigInteger p, q, g, C, r;
  private BigInteger Cr, gr;

  public NPOTSender(int numOfPairs, int msgBitLength, Connection connection) throws IOException {
    this(numOfPairs, msgBitLength, connection.getOis(), connection.getOos());
  }

  public NPOTSender(int numOfPairs, int msgBitLength, ObjectInputStream in, ObjectOutputStream out) throws IOException {
    super(numOfPairs, msgBitLength, in, out);

    initialize();
  }

  public void execProtocol(BigInteger[][] msgPairs) throws IOException {
    super.execProtocol(msgPairs);

    step1();
  }

  private void initialize() throws IOException {
    File keyfile = new File("NPOTKey");
    if (keyfile.exists()) {
      FileInputStream fin = new FileInputStream(keyfile);
      ObjectInputStream fois = new ObjectInputStream(fin);

      try {
        C = (BigInteger) fois.readObject();
        p = (BigInteger) fois.readObject();
        q = (BigInteger) fois.readObject();
        g = (BigInteger) fois.readObject();
        gr = (BigInteger) fois.readObject();
        r = (BigInteger) fois.readObject();
      } catch (ClassNotFoundException e) {
        throw new IOException("Received an unknown class type", e);
      }
      fois.close();

      oos.writeObject(C);
      oos.writeObject(p);
      oos.writeObject(q);
      oos.writeObject(g);
      oos.writeObject(gr);
      oos.writeInt(msgBitLength);
      oos.flush();

      Cr = C.modPow(r, p);
    } else {
      BigInteger pdq;
      q = new BigInteger(qLength, certainty, rnd);

      do {
        pdq = new BigInteger(pLength - qLength, rnd);
        pdq = pdq.clearBit(0);
        p = q.multiply(pdq)
            .add(BigInteger.ONE);
      } while (!p.isProbablePrime(certainty));

      do {
        g = new BigInteger(pLength - 1, rnd);
      } while ((g.modPow(pdq, p)).equals(BigInteger.ONE) || (g.modPow(q, p)).equals(BigInteger.ONE));

      r = (new BigInteger(qLength, rnd)).mod(q);
      gr = g.modPow(r, p);
      C = (new BigInteger(qLength, rnd)).mod(q);

      oos.writeObject(C);
      oos.writeObject(p);
      oos.writeObject(q);
      oos.writeObject(g);
      oos.writeObject(gr);
      oos.writeInt(msgBitLength);
      oos.flush();

      Cr = C.modPow(r, p);

      FileOutputStream fout = new FileOutputStream(keyfile);
      ObjectOutputStream foos = new ObjectOutputStream(fout);

      foos.writeObject(C);
      foos.writeObject(p);
      foos.writeObject(q);
      foos.writeObject(g);
      foos.writeObject(gr);
      foos.writeObject(r);

      foos.flush();
      foos.close();
    }
  }

  private void step1() throws IOException {
    BigInteger[] pk0;
    try {
      pk0 = (BigInteger[]) ois.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException("Received an unknown class type", e);
    }

    BigInteger[] pk1 = new BigInteger[numOfPairs];
    BigInteger[][] msg = new BigInteger[numOfPairs][2];

    for (int i = 0; i < numOfPairs; i++) {
      pk0[i] = pk0[i].modPow(r, p);
      pk1[i] = Cr.multiply(pk0[i].modInverse(p))
          .mod(p);

      msg[i][0] = Cipher.encrypt(pk0[i], msgPairs[i][0], msgBitLength);
      msg[i][1] = Cipher.encrypt(pk1[i], msgPairs[i][1], msgBitLength);
    }

    oos.writeObject(msg);
    oos.flush();
  }
}
