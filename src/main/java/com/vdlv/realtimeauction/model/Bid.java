package com.vdlv.realtimeauction.model;

import io.vertx.core.shareddata.Shareable;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Objects;

import static com.vdlv.realtimeauction.model.Util.ZERO;

/**
 * Represents a bid. It should be associated to an auction (although we haven't enforced it yet through modelling)
 */
public final class Bid implements Shareable {
  private final String buyer;
  private final BigDecimal price;
  private final ZonedDateTime time;

  /**
   * <ul>Several business rules are applied internally:
   * <li>The price is automatically set to 0 if the provided value is null or negative</li>
   * </ul>
   *
   * @param buyer must be identified
   * @param price self described
   * @throws IllegalArgumentException if buyer is null or empty (should not happen)
   */
  public Bid(String buyer, BigDecimal price) {
    if (StringUtils.isBlank(buyer)) {
      throw new IllegalArgumentException(("The buyer should be identified"));
    }
    this.buyer = buyer;

    if (price == null || ZERO.compareTo(price) == 1) {
      this.price = ZERO;
    } else {
      this.price = price.setScale(2, RoundingMode.CEILING);
    }
    this.time = Util.universalNow();
  }

  /**
   * @return The buyer
   */
  public String getBuyer() {
    return buyer;
  }

  /**
   * @return The price associated to this offer (in the current modelling it could be any currency (fiat or ether, bitcoins, socks...)
   */
  public BigDecimal getPrice() {
    return price;
  }

  /**
   * @return The time when this bid has been done (created from a technical point of view). Once again this modelling should evolved in a real use case.
   */
  public ZonedDateTime getTime() {
    return time;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Bid bid = (Bid) o;
    return Objects.equals(getBuyer(), bid.getBuyer()) &&
      Objects.equals(getPrice(), bid.getPrice()) &&
      getTime().equals(bid.getTime());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getBuyer(), getPrice(), getTime());
  }

  @Override
  public String toString() {
    return "Bid{" +
      "buyer='" + buyer + '\'' +
      ", price=" + price +
      ", time=" + time +
      '}';
  }
}
