// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main;

import crypto.ot.OTExtReceiver;
import crypto.ot.Receiver;

import java.io.IOException;
import java.net.Socket;


public abstract class ProgClient extends Program {
  public static final String DEFAULT_SERVER_IP = "localhost";             // server IP name
  public static final int DEFAULT_SERVER_PORT = 23456;                   // server port number
  private final Socket socket;

  protected int otNumOfPairs;
  protected Receiver rcver;

  public ProgClient() throws IOException {
    this(DEFAULT_SERVER_IP, DEFAULT_SERVER_PORT);
  }

  public ProgClient(String serverIp, int serverPort) throws IOException {
    this.socket = new Socket(serverIp, serverPort);
  }

  @Override
  public Connection connect() throws IOException {
    return Connection.clientInstance(socket);
  }

  @Override
  protected void initializeOT(Connection connection) throws IOException {
    connection.getOos().writeInt(otNumOfPairs);
    connection.flush();

    rcver = new OTExtReceiver(otNumOfPairs, connection);
  }
}