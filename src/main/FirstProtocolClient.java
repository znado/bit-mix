package main;

import circuit.Circuit;
import circuit.State;
import main.value.FirstProtocolClientInput;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class FirstProtocolClient extends ProtocolClient<FirstProtocolClientInput> {
  public FirstProtocolClient(final String serverIp, final int serverPort) throws IOException {
    super(serverIp, serverPort);
  }

  @Override
  protected BigInteger convert(final FirstProtocolClientInput input) {
    return input.secretAddress.shiftLeft(FirstProtocol.BIT_LENGTH).add(input.gAB);
  }

  @Override
  protected List<Circuit> createCircuits(final FirstProtocolClientInput input) {
    return FirstProtocol.getFirstProtocolCircuit(input.modulus);
  }

  @Override
  protected State execCircuit(final List<Circuit> circuits, final Connection connection, final State local,
      final State remote) {
    return FirstProtocol.execute(circuits, local, remote);
  }

  @Override
  protected int inputLength(final FirstProtocolClientInput input) {
    return FirstProtocol.BIT_LENGTH * 2;
  }
}
