package com.vdlv.realtimeauction.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    Auction auction = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100));
    assertThat(auction.getFirstPrice(), is(BigDecimal.valueOf(100)));
    assertThat(auction.getProduct(), is(PRODUCT_TEST_1));
    assertThat(auction.getId(), not(isEmptyString()));
    assertThat(auction.getEndingTime(), within(5, ChronoUnit.MINUTES, Util.universalNow()));
  }

  @Test
  @Tag("Unit")
  void ctorNullProduct() {
    Throwable thrown = assertThrows(IllegalArgumentException.class, () -> new Auction(null, BigDecimal.valueOf(100)), "Expected an IllegalArgumentException to be thrown");
    assertThat(thrown.getMessage(), is("Product description should not ne null or empty"));
  }

  @Test
  @Tag("Unit")
  void ctorEmptyProduct() {
    Throwable thrown = assertThrows(IllegalArgumentException.class, () -> new Auction("           ", BigDecimal.valueOf(100)), "Expected an IllegalArgumentException to be thrown");
    assertThat(thrown.getMessage(), is("Product description should not ne null or empty"));
  }

  @Test
  @Tag("Unit")
  void ctorNullPrice() {
    Auction auction = new Auction("OK", null);
    assertThat(auction.getFirstPrice(), is(BigDecimal.ZERO));
  }

  @Test
  @Tag("Unit")
  void ctorNegativePrice() {
    Auction auction = new Auction("OK", BigDecimal.valueOf(-1));
    assertThat(auction.getFirstPrice(), is(BigDecimal.ZERO));
  }

  @Test
  @Tag("Unit")
  void auctionsAreNeverEquals() {
    Auction auction1 = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100));
    Auction auction2 = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100));
    assertNotEquals(auction1, auction2);
  }

  @Test
  @Tag("Unit")
  void addNullBidIsIgnored() {
    Auction auction = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100));
    assertFalse(auction.addBid(null));
  }

  @Test
  @Tag("Unit")
  void addBidLowerThanFirstPriceIsIgnored() {
    Auction auction = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100));
    assertFalse(auction.addBid(new Bid(BUYER_1, BigDecimal.ZERO)));
  }

  @Test
  @Tag("Unit")
  void addBidLowerWhenAuctionIsClosedIsIgnored() {
    Auction auction = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100), Util.universalNow());
    assertFalse(auction.addBid(new Bid(BUYER_1, BigDecimal.valueOf(200))));
  }

  @Test
  @Tag("Unit")
  void addBidOKIsAccepted() {
    Auction auction = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100), Util.universalNow().plus(10, ChronoUnit.MILLIS));
    Bid bid = new Bid(BUYER_1, BigDecimal.valueOf(100));
    assertTrue(auction.addBid(bid));
  }

  @Test
  @Tag("Unit")
  void bidFirstPriceIsAccepted() {
    Auction auction = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100), Util.universalNow().plus(10, ChronoUnit.MILLIS));
    Bid bid = new Bid(BUYER_1, BigDecimal.valueOf(200));
    assertTrue(auction.addBid(bid));
    bid = new Bid(BUYER_1, BigDecimal.valueOf(200));
  }

  @Test
  @Tag("Unit")
  void bidAlreadyBiddedPriceIsRejected() {
    Auction auction = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100), Util.universalNow().plus(10, ChronoUnit.MILLIS));
    Bid bid = new Bid(BUYER_1, BigDecimal.valueOf(200));
    assertTrue(auction.addBid(bid));
    assertFalse(auction.addBid(new Bid(BUYER_2, BigDecimal.valueOf(200))));
  }

  @Test
  @Tag("Unit")
  void noBidProcess() throws InterruptedException {
    Auction auction = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100), Util.universalNow().plus(10, ChronoUnit.MILLIS));
    assertThat(auction.getCurrentAuctionValue(), is(BigDecimal.valueOf(100)));
    Thread.sleep(20);
    assertTrue(auction.isClosed());
    assertThat(auction.getCurrentAuctionValue(), is(BigDecimal.valueOf(100)));
    assertThat(auction.andTheWinnerIs(), is(Util.UnluckyAuctionBid.getBuyer()));
    assertThat(auction.getWinningBid().isPresent(), is(false));
  }

  @Test
  @Tag("Unit")
  void oneBidProcess() throws InterruptedException {
    Auction auction = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100), Util.universalNow().plus(10, ChronoUnit.MILLIS));
    Bid bid = new Bid(BUYER_1, BigDecimal.valueOf(200));
    assertTrue(auction.addBid(bid));
    Thread.sleep(20);
    assertTrue(auction.isClosed());
    assertThat(auction.andTheWinnerIs(), is(BUYER_1));
    assertThat(auction.getWinningBid().get(), is(bid));
  }

  @Test
  @Tag("Unit")
  void multiBidProcess() throws InterruptedException {
    Auction auction = new Auction(PRODUCT_TEST_1, BigDecimal.valueOf(100), Util.universalNow().plus(100, ChronoUnit.MILLIS));
    assertThat(auction.getCurrentAuctionValue(), is(BigDecimal.valueOf(100)));
    assertTrue(auction.addBid(new Bid(BUYER_1, BigDecimal.valueOf(200))));
    assertFalse(auction.addBid(new Bid(BUYER_2, BigDecimal.valueOf(200))));// Not enough
    assertThat(auction.getCurrentAuctionValue(), is(BigDecimal.valueOf(200)));

    assertTrue(auction.addBid(new Bid(BUYER_2, BigDecimal.valueOf(300))));
    assertThat(auction.getCurrentAuctionValue(), is(BigDecimal.valueOf(300)));

    assertTrue(auction.addBid(new Bid(BUYER_1, BigDecimal.valueOf(301))));
    assertThat(auction.getCurrentAuctionValue(), is(BigDecimal.valueOf(301)));

    Bid lastBid = new Bid(BUYER_2, BigDecimal.valueOf(400));
    assertTrue(auction.addBid(lastBid));
    assertThat(auction.getCurrentAuctionValue(), is(BigDecimal.valueOf(400)));


    Thread.sleep(110);
    assertTrue(auction.isClosed());
    assertThat(auction.andTheWinnerIs(), is(BUYER_2));
    assertThat(auction.getWinningBid().get(), is(lastBid));
    assertThat(auction.getCurrentAuctionValue(), is(BigDecimal.valueOf(400)));

  }

  @Test
  @Tag("Unit")
  void auctionOrdering() throws InterruptedException {
    List<Auction> auctions = Arrays.asList(new Auction(PRODUCT_TEST_1 + ".1", BigDecimal.valueOf(100), Util.universalNow().plus(100, ChronoUnit.MILLIS)), new Auction(PRODUCT_TEST_1 + ".2", BigDecimal.valueOf(100)), new Auction(PRODUCT_TEST_1 + ".3", BigDecimal.valueOf(100), Util.universalNow().minus(100, ChronoUnit.MILLIS)));
    Collections.sort(auctions);
    assertThat(auctions.get(0).getProduct(), is(PRODUCT_TEST_1 + ".2"));
    assertThat(auctions.get(1).getProduct(), is(PRODUCT_TEST_1 + ".1"));
    assertThat(auctions.get(2).getProduct(), is(PRODUCT_TEST_1 + ".3"));
  }

}
