package main;

import circuit.Circuit;
import circuit.State;
import com.google.common.collect.Lists;
import main.wrap.EXP_STEP;
import main.wrap.RUN_MULT;
import main.wrap.Selector;

import java.math.BigInteger;
import java.util.List;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public final class FirstProtocol {
  public static final int BIT_LENGTH = 16;//162;
  private static final int FIRST_EXP = 1;
  private static final int MULT = 0;
  private static final int NORMAL_EXP = 2;
  private static final int SELECTOR = 3;

  private FirstProtocol() {
    throw new AssertionError("Not meant to be instantiated");
  }

  public static State execute(final List<Circuit> circuits, final State clientSide,
      final State serverSide) {
    BigInteger[] lbs = new BigInteger[BIT_LENGTH * 2];
    BigInteger[] clbs = clientSide.toLabels();
    BigInteger[] slbs = serverSide.toLabels();

    // The lower BITLENGTH bits of each party are
    for (int i = 0; i < BIT_LENGTH; i++) {
      lbs[RUN_MULT.X(i)] = clbs[i];
      lbs[RUN_MULT.Y(i)] = slbs[i];
    }
    System.out.println("Calculating r_Ar_C");
    State multOut = circuits.get(MULT)
        .startExecuting(State.fromLabels(lbs));

    State expIn = State.extractState(multOut, BIT_LENGTH - 1, BIT_LENGTH);
    System.out.println("Calculating g^r_Ar_Br_C");
    State out = circuits.get(FIRST_EXP)
        .startExecuting(expIn);
    System.out.println("Finished with bit 1 of " + BIT_LENGTH);

    for (int i = 1; i < BIT_LENGTH; i++) {
      expIn = State.concatenate(State.extractState(multOut, BIT_LENGTH - 1 - i, BIT_LENGTH - i), out);
      out = circuits.get(NORMAL_EXP)
          .startExecuting(expIn);
      System.out.println("Finished with bit " + (i + 1) + " of " + BIT_LENGTH);
    }

    BigInteger[] powLabels = out.toLabels();
    for (int i = 0; i < BIT_LENGTH; i++) {
      lbs[RUN_MULT.X(i)] = clbs[BIT_LENGTH+i];
      lbs[RUN_MULT.Y(i)] = powLabels[i];
    }
    System.out.println("Calculating Gs_A");
    State x_A = circuits.get(MULT)
        .startExecuting(State.fromLabels(lbs));

    // Y is kept the same
    for (int i = 0; i < BIT_LENGTH; i++) {
      lbs[RUN_MULT.X(i)] = slbs[BIT_LENGTH + i];
    }
    System.out.println("Calculating Gs_C");
    State x_C = circuits.get(MULT)
        .startExecuting(State.fromLabels(lbs));
    State selected = State.concatenate(x_A, x_C);

    BigInteger[] randomness = new BigInteger[2];
    randomness[0] = clbs[BIT_LENGTH/2];
    randomness[1] = slbs[BIT_LENGTH / 2];
    State selectors = State.fromLabels(randomness);

    return circuits.get(SELECTOR).startExecuting(State.concatenate(selectors, selected));
  }

  public static List<Circuit> getFirstProtocolCircuit(BigInteger modulus, BigInteger base) {
    return Lists.newArrayList(new RUN_MULT(BIT_LENGTH, modulus), new EXP_STEP(true, BIT_LENGTH, base, modulus),
        new EXP_STEP(false, BIT_LENGTH, base, modulus), new Selector(BIT_LENGTH));
  }
}
