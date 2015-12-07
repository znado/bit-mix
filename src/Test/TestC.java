// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package test;

import main.FirstProtocol;
import main.FirstProtocolServer;
import main.SecondProtocolClient;
import main.value.FirstProtocolInput;
import main.value.SecondProtocolClientInput;
import util.Pair;

import java.math.BigInteger;
import java.util.Random;


class TestC {
  public static final BigInteger r_C = new BigInteger(FirstProtocol.BIT_LENGTH, new Random(102)).mod(TestConstants.m);
  public static final BigInteger s_C = BigInteger.valueOf(12);

  public static void main(String[] args) throws Exception {
    FirstProtocolServer server = new FirstProtocolServer(1234);
    System.out.println("Modulus: " + TestConstants.m);
    System.out.println("r_C: " + r_C);
    System.out.println("s_C: " + s_C);
    Pair<BigInteger, BigInteger> pair = server.run(FirstProtocolInput.builder()
        .setG_rb(TestConstants.g.modPow(TestB.r_B, TestConstants.m))
        .setModulus(TestConstants.m)
        .setSecretAddress(s_C)
        .setSecretRandom(r_C)
        .build());
    System.out.println("First value: " + pair.first);
    System.out.println("Second value: " + pair.second);

    BigInteger g_r_A = TestConstants.g.modPow(TestA.r_A, TestConstants.m);
    System.out.println("g^r_A: " + g_r_A);
    SecondProtocolClient client = new SecondProtocolClient("localhost", 2345);
    client.run(SecondProtocolClientInput.builder()
        .setG_ra(g_r_A)
        .setModulus(TestConstants.m)
        .setX1(pair.first)
        .setX2(pair.second)
        .setSecretRandom(r_C)
        .build());
  }
}