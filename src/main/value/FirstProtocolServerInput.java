package main.value;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class FirstProtocolServerInput {
  public final BigInteger modulus;
  public final BigInteger secretRandom;
  public final BigInteger secretAddress;

  private FirstProtocolServerInput(Builder b) {
    this.modulus = b.modulus;
    this.secretAddress = b.secretAddress;
    this.secretRandom = b.secretRandom;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private BigInteger modulus;
    private BigInteger secretRandom;
    private BigInteger secretAddress;

    public Builder() {

    }

    public Builder setModulus(final BigInteger modulus) {
      this.modulus = modulus;
      return this;
    }

    public Builder setSecretRandom(final BigInteger secretRandom) {
      this.secretRandom = secretRandom;
      return this;
    }

    public Builder setSecretAddress(final BigInteger secretAddress) {
      this.secretAddress = secretAddress;
      return this;
    }

    public FirstProtocolServerInput build() {
      checkNotNull(modulus, "modulus");
      checkNotNull(secretAddress, "secretAddress");
      checkNotNull(secretRandom, "secretRandom");
      return new FirstProtocolServerInput(this);
    }
  }
}
