// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Test;

import main.FirstProtocolServer;
import main.value.FirstProtocolInput;
import util.Pair;

import java.math.BigInteger;
import java.util.Random;


class TestAddingServer {
  public static final BigInteger g = BigInteger.valueOf(2);
  public static final BigInteger m = BigInteger.probablePrime(16, new Random(100));
  public static final BigInteger r_B = new BigInteger(16, new Random(101)).mod(m);
  public static final BigInteger s_B = BigInteger.valueOf(12);

  public static void main(String[] args) throws Exception {
    FirstProtocolServer server = new FirstProtocolServer(1234);
    System.out.println("Server random value: " + r_B);
    Pair<BigInteger, BigInteger> pair = server.run(FirstProtocolInput.builder()
        .setG_rb(g)
        .setModulus(m)
        .setSecretAddress(s_B)
        .setSecretRandom(r_B)
        .build());
    System.out.println("First value: " + pair.first);
    System.out.println("Second value: " + pair.second);
  }
}