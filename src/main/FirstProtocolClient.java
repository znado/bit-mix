package main;

import circuit.Circuit;
import circuit.State;
import main.value.FirstProtocolInput;

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
    return input.secretAddress.shiftLeft(FirstProtocol.BITLENGTH).add(input.secretRandom);
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
