package main.value;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class SecondProtocolServerInput {
  public final BigInteger modulus;
  public final BigInteger secretRandom;

  private SecondProtocolServerInput(Builder b) {
    this.modulus = b.modulus;
    this.secretRandom = b.secretRandom;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private BigInteger modulus;
    private BigInteger secretRandom;

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

    public SecondProtocolServerInput build() {
      checkNotNull(modulus, "modulus");
      checkNotNull(secretRandom, "secretRandom");
      return new SecondProtocolServerInput(this);
    }
  }
}
