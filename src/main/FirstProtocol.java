package main;

import circuit.Circuit;
import circuit.State;
import main.value.FirstProtocolInput;

import java.math.BigInteger;
import java.util.List;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public final class FirstProtocol {
  public static final int BITLENGTH = 162;

  private FirstProtocol() {
    throw new AssertionError("Not meant to be instantiated");
  }

  public static List<Circuit> getFirstProtocolCircuit(BigInteger modulus, BigInteger base) {

  }

  public static State execute(final List<Circuit> circuits, final Connection connection, final State local,
      final State remote) {
    return null;
  }
}
