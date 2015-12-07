package main.value;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class SecondProtocolClientInput {
  public final BigInteger modulus;
  public final BigInteger g_ra;
  public final BigInteger secretRandom;
  public final BigInteger x_1;
  public final BigInteger x_2;

  private SecondProtocolClientInput(Builder b) {
    this.modulus = b.modulus;
    this.g_ra = b.g_ra;
    this.secretRandom = b.secretRandom;
    this.x_1 = b.x_1;
    this.x_2 = b.x_2;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private BigInteger modulus;
    private BigInteger g_ra;
    private BigInteger secretRandom;
    private BigInteger x_1;
    private BigInteger x_2;

    public Builder() {

    }

    public Builder setModulus(final BigInteger modulus) {
      this.modulus = modulus;
      return this;
    }

    public Builder setG_ra(final BigInteger g_ra) {
      this.g_ra = g_ra;
      return this;
    }

    public Builder setSecretRandom(final BigInteger secretRandom) {
      this.secretRandom = secretRandom;
      return this;
    }

    public Builder setX1(final BigInteger x1) {
      this.x_1 = x1;
      return this;
    }

    public Builder setX2(final BigInteger x2) {
      this.x_2 = x2;
      return this;
    }

    public SecondProtocolClientInput build() {
      checkNotNull(modulus, "modulus");
      checkNotNull(g_ra, "g_ra");
      checkNotNull(secretRandom, "secretRandom");
      checkNotNull(x_1, "x_1");
      checkNotNull(x_2, "x_2");
      return new SecondProtocolClientInput(this);
    }
  }
}
