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
public final class SecondProtocol {
  public static final int BIT_LENGTH = FirstProtocol.BIT_LENGTH;//162;
  private static final int FIRST_EXP = 1;
  private static final int MULT = 0;
  private static final int NORMAL_EXP = 2;

  private SecondProtocol() {
    throw new AssertionError("Not meant to be instantiated");
  }

  public static State execute(final List<Circuit> circuits, final State clientSide,
      final State rB) {
    State base = State.extractState(clientSide, 0, BIT_LENGTH);
    State x1 = State.extractState(clientSide, BIT_LENGTH, BIT_LENGTH*2);
    State x2 = State.extractState(clientSide, BIT_LENGTH*2, BIT_LENGTH * 3);

    State[] leftExp = new State[BIT_LENGTH];
    for (int i = 0; i < BIT_LENGTH; i++) {
      leftExp[i] = State.concatenate(State.extractState(rB, BIT_LENGTH - 1 - i, BIT_LENGTH - i), base);
    }

    System.out.println("Calculating g^-r_Ar_Br_C");
    State invG = circuits.get(FIRST_EXP)
        .startExecuting(leftExp[0]);
    System.out.println("Finished with bit 1 of " + BIT_LENGTH);

    for (int i = 1; i < BIT_LENGTH; i++) {
      State expIn = State.concatenate(leftExp[i], invG);
      long time = System.currentTimeMillis();
      invG = circuits.get(NORMAL_EXP)
          .startExecuting(expIn);
      long duration = System.currentTimeMillis() - time;
      System.out.println("Finished with bit " + (i + 1) + " of " + BIT_LENGTH + " (" + duration + " ms)");
    }

    System.out.println("Calculating x_1/G");
    State s_1 = circuits.get(MULT)
        .startExecuting(State.concatenate(invG, x1));

    System.out.println("Calculating x_2/G");
    State s_2 = circuits.get(MULT)
        .startExecuting(State.concatenate(invG, x2));

    return State.concatenate(s_1, s_2);
  }

  public static List<Circuit> getSecondProtocolCircuit(BigInteger modulus) {
    return Lists.newArrayList(new RUN_MULT(BIT_LENGTH, modulus), new EXP_STEP(true, BIT_LENGTH, modulus),
        new EXP_STEP(false, BIT_LENGTH, modulus));
  }
}
