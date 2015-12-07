// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main;

import circuit.Circuit;
import circuit.Wire;
import crypto.ot.OTExtSender;
import crypto.ot.Sender;

import java.io.IOException;
import java.net.ServerSocket;


public abstract class ProgServer extends Program implements AutoCloseable {

  public static final int SERVER_PORT = 23456;             // server port number
  private Connection connection;
  protected int otMsgBitLength = Wire.labelBitLength;
  protected int otNumOfPairs;
  protected Sender snder;
  private final ServerSocket sock;              // original server socket

  public ProgServer() throws IOException {
    this(SERVER_PORT);
  }

  public ProgServer(int port) throws IOException {
    sock = new ServerSocket(port);
  }

  @Override
  public void close() throws Exception {
    sock.close();
    connection.close();
  }

  protected void createCircuits() throws Exception {
    Circuit.isForGarbling = true;
    Circuit.setIOStream(ProgCommon.ois, ProgCommon.oos);
    for (int i = 0; i < ProgCommon.ccs.length; i++) {
      ProgCommon.ccs[i].build();
    }
  }

  protected void initializeOT() throws Exception {
    otNumOfPairs = ProgCommon.ois.readInt();

    snder = new OTExtSender(otNumOfPairs, otMsgBitLength, ProgCommon.ois, ProgCommon.oos);
  }

  @Override
  public void run() throws Exception {
    connection = new Connection(sock.accept());

    super.run();
  }
}