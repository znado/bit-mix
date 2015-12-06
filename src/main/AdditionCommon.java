package main;

import circuit.Circuit;
import circuit.State;
import javafx.scene.paint.Stop;
import main.wrap.EXP_STEP;
import main.wrap.RUN_ADD1;
import main.wrap.RUN_COND_ADD;
import main.wrap.RUN_EXP;
import main.wrap.RUN_MULT;
import main.wrap.RUN_SQUARE;
import main.wrap.Wrappers;
import util.BigIntegers;
import util.StopWatch;

import java.math.BigInteger;
import java.util.Random;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class AdditionCommon extends ProgCommon {
  public static final Random random = new Random();

  public static final int BIT_LENGTH = 160;
  public static final BigInteger MODULUS = BigInteger.probablePrime(BIT_LENGTH, new Random(100 /* fixed seed */));
  public static final BigInteger MOD_NEG = BigIntegers.negative(MODULUS);
  public static final BigInteger MOD_MINUS_ONE = MODULUS.subtract(BigInteger.ONE);
  public static final BigInteger X = valueOfLength(BIT_LENGTH - 1);
  public static final BigInteger Y = valueOfLength(BIT_LENGTH - 1);
  public static final BigInteger BASE = BigInteger.probablePrime(BIT_LENGTH-1, new Random(101 /* fixed seed */));

  public static void display(String name, BigInteger b) {
    System.out.print(name);
    for (int i = name.length(); i < 10; i++) {
      System.out.print(" ");
    }
    System.out.print("\t");
    for (int i = b.bitLength(); i < BIT_LENGTH; i++) {
      System.out.print("0");
    }
    System.out.println(b.toString(2) + "\t (" + b.toString() + ")");
  }

  public static State execCircuit(BigInteger[] slbs, BigInteger[] clbs) throws Exception {
    BigInteger[] lbs = new BigInteger[2 * BIT_LENGTH];
    for (int i = 0; i < BIT_LENGTH; i++) {
      lbs[Wrappers.Y(i)] = clbs[i];
      lbs[Wrappers.X(i)] = slbs[i];
    }
    State in = State.fromLabels(lbs);
    State multOut = ccs[0].startExecuting(in);

    StopWatch.taskTimeStamp("product of x, y found");

    State expIn = State.extractState(multOut, AdditionCommon.BIT_LENGTH - 1, AdditionCommon.BIT_LENGTH);
    State out = ccs[1].startExecuting(expIn);
    StopWatch.taskTimeStamp("bit 1 of " + AdditionCommon.BIT_LENGTH);

    for (int i = 1; i < AdditionCommon.BIT_LENGTH; i++) {
      expIn = State.concatenate(out,
          State.extractState(multOut, AdditionCommon.BIT_LENGTH - 1 - i, AdditionCommon.BIT_LENGTH - i));
      out = ccs[2].startExecuting(expIn);
      StopWatch.taskTimeStamp("bit " + (i+1) + " of " + AdditionCommon.BIT_LENGTH);
    }

    return out;
  }

  protected static void initCircuits() {
    ccs = new Circuit[3];
    ccs[0] = new RUN_MULT();
    ccs[1] = new EXP_STEP(true);
    ccs[2] = new EXP_STEP(false);
  }

  public static BigInteger valueOfLength(int n) {
    return new BigInteger(n, random).setBit(n - 1);
  }
}
