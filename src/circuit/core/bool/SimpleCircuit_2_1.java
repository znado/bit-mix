// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package circuit.core.bool;

import circuit.Circuit;
import circuit.TransitiveObservable;
import circuit.Wire;
import crypto.Cipher;
import main.Connection;
import util.Utils;

import java.io.IOException;
import java.math.BigInteger;


public abstract class SimpleCircuit_2_1 extends Circuit {

  private Connection connection;
  protected BigInteger[][] gtt;

  public SimpleCircuit_2_1(String name) {
    super(2, 1, name);
  }

  public void build(Connection connection) throws IOException {
    createInputWires();
    createOutputWires();
    this.connection = connection;
  }

  protected void createInputWires() {
    super.createInputWires();

    for (int i = 0; i < inDegree; i++) {
      inputWires[i].addObserver(this, new TransitiveObservable.OSocket(inputWires, i));
    }
  }

  protected void execute() {

    Wire inWireL = inputWires[0];
    Wire inWireR = inputWires[1];
    Wire outWire = outputWires[0];

    if (inWireL.value != Wire.UNKNOWN_SIG && inWireR.value != Wire.UNKNOWN_SIG) {
      compute();
    } else if (inWireL.value != Wire.UNKNOWN_SIG) {
      if (shortCut()) {
        outWire.invd = false;
      } else {
        outWire.value = Wire.UNKNOWN_SIG;
        outWire.invd = inWireR.invd;
        outWire.setLabel(inWireR.lbl);
      }
    } else if (inWireR.value != Wire.UNKNOWN_SIG) {
      if (shortCut()) {
        outWire.invd = false;
      } else {
        outWire.value = Wire.UNKNOWN_SIG;
        outWire.invd = inWireL.invd;
        outWire.setLabel(inWireL.lbl);
      }
    } else {
      outWire.value = Wire.UNKNOWN_SIG;
      outWire.invd = false;

      if (!collapse()) {
        execYao();
      }
    }

    outWire.setReady();
  }

  protected abstract boolean collapse();

  protected void createOutputWires() {
    outputWires[0] = new Wire();
  }

  protected void encryptTruthTable() {
    Wire inWireL = inputWires[0];
    Wire inWireR = inputWires[1];
    Wire outWire = outputWires[0];

    BigInteger[] labelL = {inWireL.lbl, Wire.conjugate(inWireL.lbl)};
    if (inWireL.invd) {
      BigInteger tmp = labelL[0];
      labelL[0] = labelL[1];
      labelL[1] = tmp;
    }

    BigInteger[] labelR = {inWireR.lbl, Wire.conjugate(inWireR.lbl)};
    if (inWireR.invd) {
      BigInteger tmp = labelR[0];
      labelR[0] = labelR[1];
      labelR[1] = tmp;
    }

    int k = outWire.serialNum;

    int cL = inWireL.lbl.testBit(0) ? 1 : 0;
    int cR = inWireR.lbl.testBit(0) ? 1 : 0;

    if (cL != 0 || cR != 0) {
      gtt[cL][cR] = Cipher.encrypt(labelL[0], labelR[0], k, gtt[cL][cR]);
    }
    if (cL != 0 || cR != 1) {
      gtt[cL][1 ^ cR] = Cipher.encrypt(labelL[0], labelR[1], k, gtt[cL][1 ^ cR]);
    }
    if (cL != 1 || cR != 0) {
      gtt[1 ^ cL][cR] = Cipher.encrypt(labelL[1], labelR[0], k, gtt[1 ^ cL][cR]);
    }
    if (cL != 1 || cR != 1) {
      gtt[1 ^ cL][1 ^ cR] = Cipher.encrypt(labelL[1], labelR[1], k, gtt[1 ^ cL][1 ^ cR]);
    }
  }

  protected abstract void execYao();

  protected void receiveGTT() {
    try {
      gtt = new BigInteger[2][2];

      gtt[0][0] = BigInteger.ZERO;
      gtt[0][1] = Utils.readBigInteger(10, connection.getOis());
      gtt[1][0] = Utils.readBigInteger(10, connection.getOis());
      gtt[1][1] = Utils.readBigInteger(10, connection.getOis());
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  protected void sendGTT() {
    try {
      Utils.writeBigInteger(gtt[0][1], 10, connection.getOos());
      Utils.writeBigInteger(gtt[1][0], 10, connection.getOos());
      Utils.writeBigInteger(gtt[1][1], 10, connection.getOos());
      connection.flush();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  protected abstract boolean shortCut();
}
