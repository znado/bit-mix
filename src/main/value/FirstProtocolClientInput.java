package main.value;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class FirstProtocolClientInput {
  public final BigInteger modulus;
  public final BigInteger gAB;
  public final BigInteger secretAddress;

  private FirstProtocolClientInput(Builder b) {
    this.modulus = b.modulus;
    this.gAB = b.gAB;
    this.secretAddress = b.secretAddress;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private BigInteger modulus;
    private BigInteger gAB;
    private BigInteger secretAddress;

    public Builder() {

    }

    public Builder setModulus(final BigInteger modulus) {
      this.modulus = modulus;
      return this;
    }

    public Builder setGAB(final BigInteger gAB) {
      this.gAB = gAB;
      return this;
    }

    public Builder setSecretAddress(final BigInteger secretAddress) {
      this.secretAddress = secretAddress;
      return this;
    }

    public FirstProtocolClientInput build() {
      checkNotNull(modulus, "modulus");
      checkNotNull(gAB, "gAB");
      checkNotNull(secretAddress, "secretAddress");
      return new FirstProtocolClientInput(this);
    }
  }
}
