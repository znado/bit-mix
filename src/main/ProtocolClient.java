// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main;

import circuit.Circuit;
import circuit.State;
import circuit.Wire;
import crypto.ot.OTExtReceiver;
import crypto.ot.Receiver;
import util.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.List;


public abstract class ProtocolClient<I> {
  private final Socket socket;

  public ProtocolClient(String serverIp, int serverPort) throws IOException {
    this.socket = new Socket(serverIp, serverPort);
  }

  private static State getLocalState(Receiver receiver, BigInteger localBits) throws IOException {
    receiver.execProtocol(localBits);
    return State.fromLabels(receiver.getData());
  }

  private static State getRemoteState(Connection connection) throws IOException {
    int remoteSize = connection.getOis()
        .readInt();
    BigInteger[] serverLabels = new BigInteger[remoteSize];

    for (int i = 0; i < remoteSize; i++) {
      int byteLength = (Wire.labelBitLength - 1) / 8 + 1;
      serverLabels[i] = Utils.readBigInteger(byteLength, connection.getOis());
    }
    return State.fromLabels(serverLabels);
  }

  private static Receiver initializeOT(Connection connection, int otNumOfPairs) throws IOException {
    connection.getOos()
        .writeInt(otNumOfPairs);
    connection.flush();

    return new OTExtReceiver(otNumOfPairs, connection);
  }

  private static void transmitOutput(Connection connection, State outputState) throws IOException {
    connection.getOos()
        .writeObject(outputState.toLabels());
    connection.flush();
  }

  protected abstract BigInteger convert(final I input);

  protected abstract List<Circuit> createCircuits(I input);

  protected abstract State execCircuit(List<Circuit> circuits, Connection connection, State local, State remote);

  protected abstract int inputLength(final I input);

  public final void run(I input) throws IOException {
    try (Connection connection = Connection.clientInstance(socket)) {
      List<Circuit> circuits = createCircuits(input);
      Protocols.buildCircuits(circuits, connection);

      BigInteger inputAsBits = convert(input);
      int inputLength = inputLength(input);
      if (inputLength < inputAsBits.bitLength()) {
        throw new IllegalArgumentException("Cannot have input length shorter than actual input bytes.");
      }

      Receiver receiver = initializeOT(connection, inputLength);
      State remoteState = getRemoteState(connection);
      State localState = getLocalState(receiver, inputAsBits);

      State outputState = execCircuit(circuits, connection, localState, remoteState);
      transmitOutput(connection, outputState);
    }
  }
}