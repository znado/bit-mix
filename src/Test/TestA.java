package test;

import main.FirstProtocol;
import main.FirstProtocolClient;
import main.value.FirstProtocolClientInput;

import java.math.BigInteger;
import java.util.Random;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class TestA {
  public static final BigInteger r_A = new BigInteger(FirstProtocol.BIT_LENGTH, new Random(101)).mod(TestConstants.m);
  public static final BigInteger s_A = BigInteger.valueOf(3000000000L);

  public static void main(String[] args) throws Exception {
    FirstProtocolClient client = new FirstProtocolClient("localhost", 1234);
    System.out.println("Modulus: " + TestConstants.m);
    System.out.println("r_A: " + r_A);
    System.out.println("s_A: " + s_A);
    client.run(FirstProtocolClientInput.builder()
        .setGAB(TestConstants.g.modPow(TestB.r_B, TestConstants.m)
            .modPow(r_A, TestConstants.m))
        .setModulus(TestConstants.m)
        .setSecretAddress(s_A)
        .build());
  }
}
