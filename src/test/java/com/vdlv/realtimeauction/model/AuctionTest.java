package com.vdlv.realtimeauction.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.vdlv.realtimeauction.model.Util.*;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.exparity.hamcrest.date.ZonedDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests suite for Auction model")
class AuctionTest {

  private static final String PRODUCT_TEST_1 = "Cups of tea of the last century";
  private static final String BUYER_1 = "John Doe";
  private static final String BUYER_2 = "Jane Doe";

  @Test
  @Tag("Unit")
  void ctorHappyPath() {
    Auction auction = new Auction(PRODUCT_TEST_1, ONE_HUNDRED);
    assertThat(auction.getFirstPrice(), is(ONE_HUNDRED));
    assertThat(auction.getProduct(), is(PRODUCT_TEST_1));
    assertThat(auction.getId(), not(isEmptyString()));
    assertThat(auction.getEndingTime(), within(5, MINUTES, Util.universalNow()));
  }

  @Test
  @Tag("Unit")
  void ctorNullProduct() {
    Throwable thrown = assertThrows(IllegalArgumentException.class, () -> new Auction(null, ONE_HUNDRED), "Expected an IllegalArgumentException to be thrown");
    assertThat(thrown.getMessage(), is("Product description should not ne null or empty"));
  }

  @Test
  @Tag("Unit")
  void ctorEmptyProduct() {
    Throwable thrown = assertThrows(IllegalArgumentException.class, () -> new Auction("           ", ONE_HUNDRED), "Expected an IllegalArgumentException to be thrown");
    assertThat(thrown.getMessage(), is("Product description should not ne null or empty"));
  }

  @Test
  @Tag("Unit")
  void ctorNullPrice() {
    Auction auction = new Auction("OK", null);
    assertThat(auction.getFirstPrice(), is(Util.ZERO));
  }

  @Test
  @Tag("Unit")
  void ctorNegativePrice() {
    Auction auction = new Auction("OK", MINUS_ONE);
    assertThat(auction.getFirstPrice(), is(ZERO));
  }

  @Test
  @Tag("Unit")
  void auctionsAreNeverEquals() {
    Auction auction1 = new Auction(PRODUCT_TEST_1, ONE_HUNDRED);
    Auction auction2 = new Auction(PRODUCT_TEST_1, ONE_HUNDRED);
    assertNotEquals(auction1, auction2);
  }

  @Test
  @Tag("Unit")
  void addNullBidIsIgnored() {
    Auction auction = new Auction(PRODUCT_TEST_1, ONE_HUNDRED);
    assertFalse(auction.addBid(null));
  }

  @Test
  @Tag("Unit")
  void addBidLowerThanFirstPriceIsIgnored() {
    Auction auction = new Auction(PRODUCT_TEST_1, ONE_HUNDRED);
    assertFalse(auction.addBid(new Bid(BUYER_1, ZERO)));
  }

  @Test
  @Tag("Unit")
  void addBidLowerWhenAuctionIsClosedIsIgnored() throws InterruptedException {
    Auction auction = new Auction(PRODUCT_TEST_1, ONE_HUNDRED, Util.universalNow());
    Thread.sleep(10);
    assertFalse(auction.addBid(new Bid(BUYER_1, TWO_HUNDRED)));
  }

  @Test
  @Tag("Unit")
  void addBidOKIsAccepted() {
    Auction auction = new Auction(PRODUCT_TEST_1, ONE_HUNDRED, Util.universalNow().plus(10, MILLIS));
    Bid bid = new Bid(BUYER_1, ONE_HUNDRED);
    assertTrue(auction.addBid(bid));
  }

  @Test
  @Tag("Unit")
  void bidFirstPriceIsAccepted() {
    Auction auction = new Auction(PRODUCT_TEST_1, ONE_HUNDRED, Util.universalNow().plus(10, MILLIS));
    Bid bid = new Bid(BUYER_1, TWO_HUNDRED);
    assertTrue(auction.addBid(bid));
  }

  @Test
  @Tag("Unit")
  void bidAlreadyBiddedPriceIsRejected() {
    Auction auction = new Auction(PRODUCT_TEST_1, ONE_HUNDRED, Util.universalNow().plus(10, MILLIS));
    Bid bid = new Bid(BUYER_1, TWO_HUNDRED);
    assertTrue(auction.addBid(bid));
    assertFalse(auction.addBid(new Bid(BUYER_2, TWO_HUNDRED)));
  }

  @Test
  @Tag("Unit")
  void noBidProcess() throws InterruptedException {
    Auction auction = new Auction(PRODUCT_TEST_1, ONE_HUNDRED, Util.universalNow().plus(10, MILLIS));
    assertThat(auction.getCurrentAuctionValue(), is(ONE_HUNDRED));
    Thread.sleep(20);
    assertTrue(auction.isClosed());
    assertThat(auction.getCurrentAuctionValue(), is(ONE_HUNDRED));
    assertThat(auction.andTheWinnerIs(), is(Util.UnluckyAuctionBid.getBuyer()));
    assertThat(auction.getWinningBid().isPresent(), is(false));
  }

  @Test
  @Tag("Unit")
  void oneBidProcess() throws InterruptedException {
    Auction auction = new Auction(PRODUCT_TEST_1, ONE_HUNDRED, Util.universalNow().plus(10, MILLIS));
    Bid bid = new Bid(BUYER_1, TWO_HUNDRED);
    assertTrue(auction.addBid(bid));
    assertThat(auction.andTheWinnerIs(), is(Util.UnluckyAuctionBid.getBuyer()));// auction is not closed yet
    assertThat(auction.getCurrentBuyer(), is(BUYER_1));
    Thread.sleep(20);
    assertTrue(auction.isClosed());
    assertThat(auction.andTheWinnerIs(), is(BUYER_1));
    assertThat(auction.getCurrentBuyer(), is(BUYER_1));
    assertThat(auction.getWinningBid().get(), is(bid));
  }

  @Test
  @Tag("Unit")
  void multiBidProcess() throws InterruptedException {
    Auction auction = new Auction(PRODUCT_TEST_1, ONE_HUNDRED, Util.universalNow().plus(100, MILLIS));
    assertThat(auction.getCurrentAuctionValue(), is(ONE_HUNDRED));
    assertTrue(auction.addBid(new Bid(BUYER_1, TWO_HUNDRED)));
    assertFalse(auction.addBid(new Bid(BUYER_2, TWO_HUNDRED)));// Not enough
    assertThat(auction.getCurrentAuctionValue(), is(TWO_HUNDRED));

    assertTrue(auction.addBid(new Bid(BUYER_2, THREE_HUNDRED)));
    assertThat(auction.getCurrentAuctionValue(), is(THREE_HUNDRED));

    assertTrue(auction.addBid(new Bid(BUYER_1, THREE_HUNDRED_ONE)));
    assertThat(auction.getCurrentAuctionValue(), is(THREE_HUNDRED_ONE));

    Bid lastBid = new Bid(BUYER_2, FOUR_HUNDRED);
    assertTrue(auction.addBid(lastBid));
    assertThat(auction.getCurrentAuctionValue(), is(FOUR_HUNDRED));


    Thread.sleep(110);
    assertTrue(auction.isClosed());
    assertThat(auction.andTheWinnerIs(), is(BUYER_2));
    assertThat(auction.getWinningBid().get(), is(lastBid));
    assertThat(auction.getCurrentAuctionValue(), is(FOUR_HUNDRED));

  }

  @Test
  @Tag("Unit")
  void auctionOrdering() {
    List<Auction> auctions = Arrays.asList(new Auction(PRODUCT_TEST_1 + ".1", ONE_HUNDRED, Util.universalNow().plus(100, MILLIS)), new Auction(PRODUCT_TEST_1 + ".2", ONE_HUNDRED), new Auction(PRODUCT_TEST_1 + ".3", ONE_HUNDRED, Util.universalNow().minus(100, MILLIS)));
    Collections.sort(auctions);
    assertThat(auctions.get(0).getProduct(), is(PRODUCT_TEST_1 + ".2"));
    assertThat(auctions.get(1).getProduct(), is(PRODUCT_TEST_1 + ".1"));
    assertThat(auctions.get(2).getProduct(), is(PRODUCT_TEST_1 + ".3"));
  }

}
