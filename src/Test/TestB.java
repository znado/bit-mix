// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package test;

import main.SecondProtocol;
import main.SecondProtocolServer;
import main.value.SecondProtocolServerInput;
import util.Pair;

import java.math.BigInteger;
import java.util.Random;


class TestB {
  public static final BigInteger r_B = new BigInteger(SecondProtocol.BIT_LENGTH, new Random(103)).mod(TestConstants.m);
  public static final BigInteger s_B = BigInteger.valueOf(50);

  public static void main(String[] args) throws Exception {
    SecondProtocolServer server = new SecondProtocolServer(2345);
    System.out.println("Modulus: " + TestConstants.m);
    System.out.println("r_B: " + r_B);
    System.out.println("s_B: " + s_B);
    Pair<BigInteger, BigInteger> pair = server.run(SecondProtocolServerInput.builder()
        .setG_ra(TestConstants.g.modPow(TestA.r_A, TestConstants.m))
        .setModulus(TestConstants.m)
        .setSecretRandom(r_B)
        .build());
    System.out.println("Either s_A or s_C: " + pair.first);
    System.out.println("Either s_A or s_C: " + pair.second);
  }
}