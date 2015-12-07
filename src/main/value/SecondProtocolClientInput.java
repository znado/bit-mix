package main.value;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class SecondProtocolClientInput {
  public final BigInteger modulus;
  public final BigInteger negGAC;
  public final BigInteger x_1;
  public final BigInteger x_2;

  private SecondProtocolClientInput(Builder b) {
    this.modulus = b.modulus;
    this.negGAC = b.negGAC;
    this.x_1 = b.x_1;
    this.x_2 = b.x_2;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private BigInteger modulus;
    private BigInteger negGAC;
    private BigInteger x_1;
    private BigInteger x_2;

    public Builder() {

    }

    public Builder setModulus(final BigInteger modulus) {
      this.modulus = modulus;
      return this;
    }

    public Builder setNegGAC(final BigInteger negGAC) {
      this.negGAC = negGAC;
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
      checkNotNull(negGAC, "negGAC");
      checkNotNull(x_1, "x_1");
      checkNotNull(x_2, "x_2");
      return new SecondProtocolClientInput(this);
    }
  }
}
