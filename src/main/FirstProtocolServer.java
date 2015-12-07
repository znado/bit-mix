package main;

import circuit.Circuit;
import circuit.State;
import main.value.FirstProtocolClientInput;
import main.value.FirstProtocolServerInput;
import util.Pair;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class FirstProtocolServer extends ProtocolServer<FirstProtocolServerInput, Pair<BigInteger, BigInteger>> {
  public FirstProtocolServer(final int port) throws IOException {
    super(port);
  }

  @Override
  protected BigInteger convert(final FirstProtocolServerInput input) {
    return input.secretAddress.shiftLeft(FirstProtocol.BIT_LENGTH)
        .add(input.secretRandom);
  }

  @Override
  public List<Circuit> createCircuits(final FirstProtocolServerInput input) {
    return FirstProtocol.getFirstProtocolCircuit(input.modulus);
  }

  @Override
  protected State execCircuit(final List<Circuit> circuits, final Connection connection, final State local,
      final State remote) {
    return FirstProtocol.execute(circuits, remote, local);
  }

  @Override
  protected int inputLength(final FirstProtocolServerInput input) {
    return FirstProtocol.BIT_LENGTH * 2;
  }

  @Override
  protected Pair<BigInteger, BigInteger> interpretResult(final BigInteger result) {
    return Pair.of(result.mod(BigInteger.ZERO.setBit(FirstProtocol.BIT_LENGTH)), result.shiftRight(FirstProtocol.BIT_LENGTH));
  }
}
