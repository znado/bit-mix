package main;

import circuit.Circuit;
import circuit.State;
import main.value.SecondProtocolServerInput;
import util.Pair;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class SecondProtocolServer extends ProtocolServer<SecondProtocolServerInput, Pair<BigInteger, BigInteger>> {
  public SecondProtocolServer(final int port) throws IOException {
    super(port);
  }

  @Override
  protected BigInteger convert(final SecondProtocolServerInput input) {
    return input.secretRandom;
  }

  @Override
  public List<Circuit> createCircuits(final SecondProtocolServerInput input) {
    return SecondProtocol.getSecondProtocolCircuit(input.modulus);
  }

  @Override
  protected State execCircuit(final List<Circuit> circuits, final Connection connection, final State local,
      final State remote) {
    return SecondProtocol.execute(circuits, remote, local);
  }

  @Override
  protected int inputLength(final SecondProtocolServerInput input) {
    return SecondProtocol.BIT_LENGTH;
  }

  @Override
  protected Pair<BigInteger, BigInteger> interpretResult(final BigInteger result) {
    return Pair.of(result.mod(BigInteger.ZERO.setBit(SecondProtocol.BIT_LENGTH)), result.shiftRight(SecondProtocol.BIT_LENGTH));
  }
}
