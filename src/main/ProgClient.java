// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main;

import circuit.Circuit;
import crypto.ot.OTExtReceiver;
import crypto.ot.Receiver;
import util.StopWatch;

import java.io.IOException;
import java.net.Socket;


public abstract class ProgClient extends Program implements AutoCloseable {

  public static final String DEFAULT_SERVER_IP = "localhost";             // server IP name
  public static final int DEFAULT_SERVER_PORT = 23456;                   // server port number
  private final Connection connection;

  protected int otNumOfPairs;
  protected Receiver rcver;

  public ProgClient() throws IOException {
    this(DEFAULT_SERVER_IP, DEFAULT_SERVER_PORT);
  }

  public ProgClient(String serverIp, int serverPort) throws IOException {
    this.connection = new Connection(new Socket(serverIp, serverPort));
  }

  @Override
  public void close() throws Exception {
    connection.close();
  }

  protected void createCircuits() throws Exception {
    Circuit.isForGarbling = false;
    Circuit.setIOStream(ProgCommon.ois, ProgCommon.oos);
    for (int i = 0; i < ProgCommon.ccs.length; i++) {
      ProgCommon.ccs[i].build();
    }

    StopWatch.taskTimeStamp("circuit preparation");
  }

  protected void initializeOT() throws Exception {
    ProgCommon.oos.writeInt(otNumOfPairs);
    ProgCommon.oos.flush();

    rcver = new OTExtReceiver(otNumOfPairs, ProgCommon.ois, ProgCommon.oos);
    StopWatch.taskTimeStamp("OT preparation");
  }
}