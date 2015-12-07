package main;

import circuit.Circuit;
import circuit.State;
import circuit.Wire;
import main.value.FirstProtocolInput;
import util.Nothing;
import util.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class FirstProtocolClient extends ProtocolClient<FirstProtocolInput> {
  public FirstProtocolClient(final String serverIp, final int serverPort) throws IOException {
    super(serverIp, serverPort);
  }

  @Override
  protected BigInteger convert(final FirstProtocolInput input) {
    return input.secretRandom.shiftLeft(FirstProtocol.BITLENGTH).add(input.secretAddress);
  }

  @Override
  protected List<Circuit> createCircuits(final FirstProtocolInput input) {
    return FirstProtocol.getFirstProtocolCircuit(input.modulus, input.g_rb);
  }

  @Override
  protected State execCircuit(final List<Circuit> circuits, final Connection connection, final State local,
      final State remote) {
    return FirstProtocol.execute(circuits, connection, local, remote);
  }

  @Override
  protected int inputLength(final FirstProtocolInput input) {
    return FirstProtocol.BITLENGTH * 2;
  }
}
