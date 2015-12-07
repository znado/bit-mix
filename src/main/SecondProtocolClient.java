package main;

import circuit.Circuit;
import circuit.State;
import main.value.SecondProtocolClientInput;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class SecondProtocolClient extends ProtocolClient<SecondProtocolClientInput> {
  public SecondProtocolClient(final String serverIp, final int serverPort) throws IOException {
    super(serverIp, serverPort);
  }

  @Override
  protected BigInteger convert(final SecondProtocolClientInput input) {
    return input.x_1.shiftLeft(SecondProtocol.BIT_LENGTH*2).add(input.x_2.shiftLeft(SecondProtocol.BIT_LENGTH)).add(
        input.negGAC);
  }

  @Override
  protected List<Circuit> createCircuits(final SecondProtocolClientInput input) {
    return SecondProtocol.getSecondProtocolCircuit(input.modulus);
  }

  @Override
  protected State execCircuit(final List<Circuit> circuits, final Connection connection, final State local,
      final State remote) {
    return SecondProtocol.execute(circuits, local, remote);
  }

  @Override
  protected int inputLength(final SecondProtocolClientInput input) {
    return SecondProtocol.BIT_LENGTH*3;
  }
}
