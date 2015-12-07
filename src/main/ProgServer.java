// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main;

import circuit.Wire;
import crypto.ot.OTExtSender;
import crypto.ot.Sender;

import java.io.IOException;
import java.net.ServerSocket;


public abstract class ProgServer extends Program implements AutoCloseable {

  public static final int SERVER_PORT = 23456;             // server port number
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
  }

  @Override
  public Connection connect() throws IOException {
    return Connection.serverInstance(sock.accept());
  }

  protected void initializeOT() throws Exception {
    otNumOfPairs = ProgCommon.ois.readInt();

    snder = new OTExtSender(otNumOfPairs, otMsgBitLength, ProgCommon.ois, ProgCommon.oos);
  }
}