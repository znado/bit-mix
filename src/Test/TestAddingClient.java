package Test;

import main.FirstProtocol;
import main.FirstProtocolClient;
import main.value.FirstProtocolInput;

import java.math.BigInteger;
import java.util.Random;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class TestAddingClient {
  public static final BigInteger g = BigInteger.valueOf(2);
  public static final BigInteger m = BigInteger.probablePrime(FirstProtocol.BIT_LENGTH, new Random(100));
  public static final BigInteger r_A = new BigInteger(FirstProtocol.BIT_LENGTH, new Random(101)).mod(m);
  public static final BigInteger s_A = BigInteger.valueOf(15);

  public static void main(String[] args) throws Exception {
    FirstProtocolClient client = new FirstProtocolClient("localhost", 1234);
    System.out.println("Modulus: " + m);
    System.out.println("Client random value: " + r_A);
    System.out.println("Server secret value: " + s_A);
    client.run(FirstProtocolInput.builder()
        .setG_rb(g)
        .setModulus(m)
        .setSecretAddress(s_A)
        .setSecretRandom(r_A)
        .build());
  }
}
