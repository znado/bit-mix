// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package main;

import circuit.Circuit;
import circuit.State;
import circuit.Wire;
import crypto.ot.OTExtSender;
import crypto.ot.Sender;
import util.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.util.List;


public abstract class ProtocolServer<I, O> implements AutoCloseable {
  private final ServerSocket sock;

  public ProtocolServer(int port) throws IOException {
    sock = new ServerSocket(port);
  }

  private static State doOT(final Sender sender, final BigInteger[][] clientLabelPairs) throws IOException {
    sender.execProtocol(clientLabelPairs);
    BigInteger[] zeros = new BigInteger[clientLabelPairs.length];
    for (int i = 0; i < clientLabelPairs.length; i++) {
      zeros[i] = clientLabelPairs[i][0];
    }
    return State.fromLabels(zeros);
  }

  private static Sender initializeOT(Connection connection) throws IOException {
    return new OTExtSender(connection.getOis()
        .readInt(), Wire.labelBitLength, connection);
  }

  private static BigInteger[][] labelPairs(final int numOfPairs) {
    BigInteger[][] ret = new BigInteger[numOfPairs][2];
    for (int i = 0; i < numOfPairs; i++) {
      ret[i] = Wire.newLabelPair();
    }
    return ret;
  }

  private static BigInteger receiveFinal(final Connection connection, final State outputState) throws IOException {
    BigInteger[] outLabels;
    try {
      outLabels = (BigInteger[]) connection.getOis()
          .readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }

    BigInteger output = BigInteger.ZERO;
    for (int i = 0; i < outLabels.length; i++) {
      if (outputState.wires[i].value != Wire.UNKNOWN_SIG) {
        if (outputState.wires[i].value == 1) {
          output = output.setBit(i);
        }
      } else if (outLabels[i].equals(outputState.wires[i].invd ? outputState.wires[i].lbl
          : outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1)
              .setBit(0)))) {
        output = output.setBit(i);
      } else if (!outLabels[i].equals(outputState.wires[i].invd ? outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1)
          .setBit(0)) : outputState.wires[i].lbl)) {
        System.err.println(
            "Output so far: " + output.toString(2) + " (" + (i - 1) + " of " + outLabels.length + " bits)");
        throw new IllegalStateException("Bad label encountered: i = " + i + "\t" +
            outLabels[i] + " != (" +
            outputState.wires[i].lbl + ", " +
            outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1)
                .setBit(0)) + ")");
      }
    }
    return output;
  }

  private static State sendLocal(final Connection connection, final BigInteger[][] serverLabelPairs,
      final BigInteger localState) throws IOException {
    BigInteger[] zeros = new BigInteger[serverLabelPairs.length];
    connection.getOos()
        .writeInt(serverLabelPairs.length);
    for (int i = 0; i < serverLabelPairs.length; i++) {
      int bit = localState.testBit(i) ? 1 : 0;

      int byteLength = (Wire.labelBitLength - 1) / 8 + 1;
      zeros[i] = serverLabelPairs[i][0];
      Utils.writeBigInteger(serverLabelPairs[i][bit], byteLength, connection.getOos());
    }
    connection.flush();
    return State.fromLabels(zeros);
  }

  @Override
  public void close() throws Exception {
    sock.close();
  }

  protected abstract BigInteger convert(I input);

  public abstract List<Circuit> createCircuits(I input);

  protected abstract State execCircuit(List<Circuit> circuits, Connection connection, State local, State remote);

  protected abstract int inputLength(I input);

  protected abstract O interpretResult(BigInteger result);

  public final O run(I input) throws IOException {
    try (Connection connection = Connection.serverInstance(sock.accept())) {
      List<Circuit> circuits = createCircuits(input);
      Protocols.buildCircuits(circuits, connection);

      Sender sender = initializeOT(connection);
      int inputLength = inputLength(input);
      BigInteger[][] serverLabelPairs = labelPairs(inputLength);
      BigInteger[][] clientLabelPairs = labelPairs(sender.getNumOfPairs());

      BigInteger localState = convert(input);
      State fakeLocal = sendLocal(connection, serverLabelPairs, localState);
      State fakeRemote = doOT(sender, clientLabelPairs);

      State fakeOutput = execCircuit(circuits, connection, fakeLocal, fakeRemote);
      return interpretResult(receiveFinal(connection, fakeOutput));
    }
  }
}