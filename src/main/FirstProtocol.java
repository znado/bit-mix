package main;

import circuit.Circuit;
import circuit.State;
import com.google.common.collect.Lists;
import main.wrap.EXP_STEP;
import main.wrap.RUN_MULT;

import java.math.BigInteger;
import java.util.List;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public final class FirstProtocol {
  public static final int BITLENGTH = 15;//162;
  private static final int FIRST_EXP = 1;
  private static final int MULT = 0;
  private static final int NORMAL_EXP = 2;

  private FirstProtocol() {
    throw new AssertionError("Not meant to be instantiated");
  }

  public static State execute(final List<Circuit> circuits, final Connection connection, final State clientSide,
      final State serverSide) {
    BigInteger[] lbs = new BigInteger[BITLENGTH * 2];
    BigInteger[] clbs = clientSide.toLabels();
    BigInteger[] slbs = serverSide.toLabels();
    for (int i = 0; i < BITLENGTH * 2; i++) {
      lbs[RUN_MULT.Y(i)] = clbs[i];
      lbs[RUN_MULT.X(i)] = slbs[i];
    }
    State in = State.fromLabels(lbs);
    State multOut = circuits.get(MULT)
        .startExecuting(in);

    State expIn = State.extractState(multOut, BITLENGTH - 1, BITLENGTH);
    State out = circuits.get(FIRST_EXP)
        .startExecuting(expIn);

    for (int i = 1; i < BITLENGTH; i++) {
      expIn = State.concatenate(out, State.extractState(multOut, BITLENGTH - 1 - i, BITLENGTH - i));
      out = circuits.get(NORMAL_EXP)
          .startExecuting(expIn);
      System.out.println("bit " + (i + 1) + " of " + BITLENGTH);
    }

    return out;
  }

  public static List<Circuit> getFirstProtocolCircuit(BigInteger modulus, BigInteger base) {
    return Lists.newArrayList(new RUN_MULT(BITLENGTH, modulus), new EXP_STEP(true, BITLENGTH, modulus, base),
        new EXP_STEP(false, BITLENGTH, modulus, base));
  }
}
