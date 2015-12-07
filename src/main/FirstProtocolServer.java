package main;

import circuit.Circuit;
import circuit.State;
import main.value.FirstProtocolInput;
import util.Pair;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class FirstProtocolServer extends ProtocolServer<FirstProtocolInput, Pair<BigInteger, BigInteger>> {
  public FirstProtocolServer(final int port) throws IOException {
    super(port);
  }

  @Override
  protected BigInteger convert(final FirstProtocolInput input) {
    return input.secretRandom.shiftLeft(FirstProtocol.BITLENGTH)
        .add(input.secretAddress);
  }

  @Override
  public List<Circuit> createCircuits(final FirstProtocolInput input) {
    return FirstProtocol.getFirstProtocolCircuit(input.modulus, input.g_rb);
  }

  @Override
  protected State execCircuit(final List<Circuit> circuits, final Connection connection, final State local,
      final State remote) {
    return FirstProtocol.execute(circuits, connection, remote, local);
  }

  @Override
  protected int inputLength(final FirstProtocolInput input) {
    return FirstProtocol.BITLENGTH * 2;
  }

  @Override
  protected Pair<BigInteger, BigInteger> interpretResult(final BigInteger result) {
    return Pair.of(result.mod(BigInteger.ZERO.setBit(FirstProtocol.BITLENGTH+1)), result.shiftRight(FirstProtocol.BITLENGTH));
  }
}
