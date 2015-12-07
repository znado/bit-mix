package main.value;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class FirstProtocolInput {
  public final BigInteger modulus;
  public final BigInteger g_rb;
  public final BigInteger secretRandom;
  public final BigInteger secretAddress;

  private FirstProtocolInput(Builder b) {
    this.modulus = b.modulus;
    this.g_rb = b.g_rb;
    this.secretAddress = b.secretAddress;
    this.secretRandom = b.secretRandom;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private BigInteger modulus;
    private BigInteger g_rb;
    private BigInteger secretRandom;
    private BigInteger secretAddress;

    public Builder() {

    }

    public void setModulus(final BigInteger modulus) {
      this.modulus = modulus;
    }

    public void setG_rb(final BigInteger g_rb) {
      this.g_rb = g_rb;
    }

    public void setSecretRandom(final BigInteger secretRandom) {
      this.secretRandom = secretRandom;
    }

    public void setSecretAddress(final BigInteger secretAddress) {
      this.secretAddress = secretAddress;
    }

    public FirstProtocolInput build() {
      checkNotNull(modulus, "modulus");
      checkNotNull(g_rb, "g_rb");
      checkNotNull(secretAddress, "secretAddress");
      checkNotNull(secretRandom, "secretRandom");
      return new FirstProtocolInput(this);
    }
  }
}
