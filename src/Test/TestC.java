// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package test;

import main.FirstProtocol;
import main.FirstProtocolServer;
import main.SecondProtocolClient;
import main.value.FirstProtocolServerInput;
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
    Pair<BigInteger, BigInteger> pair = server.run(FirstProtocolServerInput.builder()
        .setModulus(TestConstants.m)
        .setSecretAddress(s_C)
        .setSecretRandom(r_C)
        .build());
    System.out.println("First value: " + pair.first);
    System.out.println("Second value: " + pair.second);

    BigInteger negGAC = TestConstants.g.modPow(TestA.r_A, TestConstants.m)
        .modPow(r_C, TestConstants.m)
        .modInverse(TestConstants.m);
    System.out.println("g^-AC: " + negGAC);
    SecondProtocolClient client = new SecondProtocolClient("localhost", 2345);
    client.run(SecondProtocolClientInput.builder()
        .setNegGAC(negGAC)
        .setModulus(TestConstants.m)
        .setX1(pair.first)
        .setX2(pair.second)
        .build());
  }
}