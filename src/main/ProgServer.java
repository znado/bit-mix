// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main;

import circuit.Circuit;
import circuit.State;
import circuit.Wire;
import crypto.ot.OTExtSender;
import crypto.ot.Sender;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;


public abstract class ProgServer<I, O> extends Program<I, O> implements AutoCloseable {

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

  private void buildCircuits(List<Circuit> circuits, Connection connection) throws IOException {
    for (Circuit circuit : circuits) {
      circuit.build(connection);
    }
  }

  @Override
  public void close() throws Exception {
    sock.close();
  }

  public Connection connect() throws IOException {
    return Connection.serverInstance(sock.accept());
  }

  protected abstract State convert(I input);
  public abstract List<Circuit> createCircuits(I input);
  protected abstract State execCircuit(List<Circuit> circuits, Connection connection, State local, State remote);
  protected abstract State execTransfer(Connection connection, State inputState) throws IOException;

  protected void initializeOT(Connection connection) throws IOException {
    otNumOfPairs = connection.getOis()
        .readInt();
    snder = new OTExtSender(otNumOfPairs, otMsgBitLength, connection);
  }

  protected abstract O interpretResult(State finalState);

  public final O run(I input) throws IOException {
    try (Connection connection = connect()) {
      List<Circuit> circuits = createCircuits(input);
      buildCircuits(circuits, connection);
      initializeOT(connection);
      State inputState = convert(input);
      State transferState = execTransfer(connection, inputState);
      State finalState = execCircuit(circuits, connection, inputState, transferState);
      return interpretResult(finalState);
    }
  }
}