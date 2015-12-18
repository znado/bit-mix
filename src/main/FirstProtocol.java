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
  public static final int BIT_LENGTH = 162;
  private static final int FIRST_EXP = 1;
  private static final int MULT = 0;
  private static final int NORMAL_EXP = 2;
  private static final int SELECTOR = 3;

  private FirstProtocol() {
    throw new AssertionError("Not meant to be instantiated");
  }

  public static State execute(final List<Circuit> circuits, final State clientSide,
      final State serverSide) {
    State base = State.extractState(clientSide, 0, BIT_LENGTH);
    State sA = State.extractState(clientSide, BIT_LENGTH, BIT_LENGTH*2);
    State rC = State.extractState(serverSide, 0, BIT_LENGTH);
    State sC = State.extractState(serverSide, BIT_LENGTH, BIT_LENGTH*2);

    State[] leftExp = new State[BIT_LENGTH];
    for (int i = 0; i < BIT_LENGTH; i++) {
      leftExp[i] = State.concatenate(State.extractState(rC, BIT_LENGTH - 1 - i, BIT_LENGTH - i), base);
    }

    System.out.println("Calculating g^r_Ar_Br_C");
    State G = circuits.get(FIRST_EXP)
        .startExecuting(leftExp[0]);
    System.out.println("Finished with bit 1 of " + BIT_LENGTH);

    for (int i = 1; i < BIT_LENGTH; i++) {
      State expIn = State.concatenate(leftExp[i], G);
      long time = System.currentTimeMillis();
      G = circuits.get(NORMAL_EXP)
          .startExecuting(expIn);
      long duration = System.currentTimeMillis() - time;
      System.out.println("Finished with bit " + (i + 1) + " of " + BIT_LENGTH + " (" + duration + " ms)");
    }

    System.out.println("Calculating Gs_A");
    State x_A = circuits.get(MULT)
        .startExecuting(State.concatenate(G, sA));

    System.out.println("Calculating Gs_C");
    State x_C = circuits.get(MULT)
        .startExecuting(State.concatenate(G, sC));
    State selected = State.concatenate(x_A, x_C);

    BigInteger[] randomness = new BigInteger[2];
    randomness[0] = rC.labelAt(BIT_LENGTH/2);
    randomness[1] = base.labelAt(BIT_LENGTH / 2);
    State selectors = State.fromLabels(randomness);

    return circuits.get(SELECTOR).startExecuting(State.concatenate(selectors, selected));
  }

  public static List<Circuit> getFirstProtocolCircuit(BigInteger modulus) {
    return Lists.newArrayList(new RUN_MULT(BIT_LENGTH, modulus), new EXP_STEP(true, BIT_LENGTH, modulus),
        new EXP_STEP(false, BIT_LENGTH, modulus), new Selector(BIT_LENGTH));}
}
