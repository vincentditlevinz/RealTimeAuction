package com.vdlv.realtimeauction.model;

import io.vertx.core.shareddata.Shareable;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents an auction in this system.
 */
public final class Auction implements Shareable, Comparable<Auction> {
  private final String id;
  private final String product;
  private ZonedDateTime endingTime;
  private final BigDecimal firstPrice;
  private final ArrayDeque<Bid> bids;

  /**
   * Create a new Auction from a technical <b>and business</b> point of view.
   * <ul>Several business rules are applied internally:
   * <li>An id is automatically affected to this auction</li>
   * <li>Auction ending time is set automatically to now + 5 minutes</li>
   * <li>The first price is automatically set to 0 if the provided value is null or negative</li>
   * </ul>
   *
   * @param product    a description of the product
   * @param firstPrice the first price for this sell
   * @throws IllegalArgumentException if product is null or empty (should not happen)
   */
  public Auction(String product, BigDecimal firstPrice) {
    this(product, firstPrice, Util.universalNow().plusMinutes(Util.auctionValidityInMinutes()));
  }

  /**
   * Create a new Auction from a technical <b>and business</b> point of view.
   * <ul>Several business rules are applied internally:
   * <li>An id is automatically affected to this auction</li>
   * <li>The first price is automatically set to 0 if the provided value is null or negative</li>
   * </ul>
   *
   * @param product    a description of the product
   * @param firstPrice the first price for this sell
   * @param endingTime the time when the auction will be closed
   */
  public Auction(String product, BigDecimal firstPrice, ZonedDateTime endingTime) {
    this.id = UUID.randomUUID().toString();
    this.product = product;
    if (StringUtils.isBlank(product)) {
      throw new IllegalArgumentException(("Product description should not ne null or empty"));
    }
    this.endingTime = endingTime;
    if (firstPrice == null || BigDecimal.ZERO.compareTo(firstPrice) == 1) {
      this.firstPrice = BigDecimal.ZERO;
    } else {
      this.firstPrice = firstPrice;
    }
    this.bids = new ArrayDeque<>();
  }

  /**
   * A full ctor that is used for {@link Shareable#copy()}
   *
   * @param id auction id
   * @param product the product detail
   * @param endingTime the auction's ending time
   * @param firstPrice the first price
   * @param bids a stack of bids
   */
  private Auction(String id, String product, ZonedDateTime endingTime, BigDecimal firstPrice, ArrayDeque<Bid> bids) {
    this.id = id;
    this.product = product;
    this.endingTime = endingTime;
    this.firstPrice = firstPrice;
    this.bids = bids;
  }

  /**
   * @return the auction id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the product description (a more involved model can be provided in future releases)
   */
  public String getProduct() {
    return product;
  }

  /**
   * @return when the auction will be closed.
   */
  public ZonedDateTime getEndingTime() {
    return endingTime;
  }

  /**
   * @return the first price (e.g. the minimum acceptable)
   */
  public BigDecimal getFirstPrice() {
    return firstPrice;
  }

  /**
   * @return the buyer when the auction is closed
   */
  public String andTheWinnerIs() {
    return getWinningBid().orElse(Util.UnluckyAuctionBid).getBuyer();
  }

  /**
   * @return the current buyer
   */
  public String getCurrentBuyer() {
    return Optional.ofNullable(bids.peekLast()).orElse(Util.UnluckyAuctionBid).getBuyer();
  }

  /**
   * @return The last Bid is the auction is closed. An empty result id provided if the auction is still open or if there
   * is no bid for this auction.
   */
  public Optional<Bid> getWinningBid() {
    if (isClosed()) {
      return Optional.ofNullable(bids.peekLast());
    } else {
      return Optional.empty();
    }

  }

  private boolean isClosed(ZonedDateTime time) {
    return getEndingTime().isBefore(time);
  }

  public boolean isClosed() {
    return isClosed(Util.universalNow());
  }

  public boolean isOpen() {
    return !isClosed();
  }


  /**
   * Add a bid to this auction if is still possible.
   * A bid is acceptable if it is not null, not outdated (see {@link #isBidOutdated})
   * and corresponds to the best price (see {@link #isTheBestPrice}).
   * A bid is recordable if one can persist it in the store.
   *
   * @param bid a provided bid
   * @return true if the bid is accepted (acceptable and recordable)
   */
  public boolean addBid(Bid bid) {
    if (bid != null && !isBidOutdated(bid) && isTheBestPrice(bid)) {
      return bids.add(bid);
    } else {
      return false;
    }
  }

  /**
   * @return the value of the auction 'now'
   */
  public BigDecimal getCurrentAuctionValue() {
    if (bids.isEmpty()) {
      return firstPrice;
    } else {
      return bids.peekLast().getPrice();
    }
  }

  /**
   * Checks if the bid is outdated (it was made after the auction end time)
   *
   * @param bid a provided bid
   * @return true if the bid is outdated false otherwise
   */
  public boolean isBidOutdated(Bid bid) {
    return isClosed(bid.getTime());
  }

  /**
   * Checks if the bid's price is the best one.
   * <ul>Different possible cases:
   * <li>It is the first bid, then the bid's price should be higher than or equals to the first price associated to the auction</li>
   * <li>Other bids have been made, then the bid's price must be higher than the last bid already recorded</li>
   * </ul>
   *
   * @param bid a provided bid
   * @return true if the bid win false otherwise
   */
  public boolean isTheBestPrice(Bid bid) {
    if (bids.isEmpty()) {
      return bid.getPrice().compareTo(getFirstPrice()) >= 0;// can be equal to the first price
    } else {
      final BigDecimal currentBestPrice = bids.peekLast().getPrice();
      return bid.getPrice().compareTo(currentBestPrice) == 1;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Auction auction = (Auction) o;
    return getId().equals(auction.getId()) &&
      getProduct().equals(auction.getProduct()) &&
      getEndingTime().equals(auction.getEndingTime());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getProduct(), getEndingTime());
  }

  @Override
  public String toString() {
    return "Auction{" +
      "id='" + id + '\'' +
      ", product='" + product + '\'' +
      ", endingTime=" + endingTime +
      ", firstPrice=" + firstPrice +
      '}';
  }

  @Override
  public Shareable copy() {
    return new Auction(id, product, endingTime, firstPrice, bids.clone());// A Bid object is immutable, thus cloning should be OK
  }

  @Override
  public int compareTo(Auction auction) {// a useful default ordering from the most recent to the oldest
    if (auction == null) {
      throw new NullPointerException("Provided auction object could not be null according to the specification.");
    }
    return -getEndingTime().compareTo(auction.getEndingTime());
  }
}
