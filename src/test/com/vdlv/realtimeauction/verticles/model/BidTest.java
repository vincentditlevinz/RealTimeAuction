package com.vdlv.realtimeauction.verticles.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

import static org.exparity.hamcrest.date.ZonedDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Tests suite for Bid model")
class BidTest {

  private static final String BUYER_TEST = "John Doe";

  @Test
  @Tag("Unit")
  void ctorHappyPath() {
    Bid bid = new Bid(BUYER_TEST, BigDecimal.valueOf(100));
    assertThat(bid.getPrice(), is(BigDecimal.valueOf(100)));
    assertThat(bid.getBuyer(), is(BUYER_TEST));
    assertThat(bid.getTime(), within(1, ChronoUnit.SECONDS, Util.universalNow()));
  }

  @Test
  @Tag("Unit")
  void ctorNullBuyer() {
    Throwable thrown = assertThrows(IllegalArgumentException.class, () -> new Bid(null, BigDecimal.valueOf(100)), "Expected an IllegalArgumentException to be thrown");
    assertThat(thrown.getMessage(), is("The buyer should be identified"));
  }

  @Test
  @Tag("Unit")
  void ctorEmptyBuyer() {
    Throwable thrown = assertThrows(IllegalArgumentException.class, () -> new Bid("           ", BigDecimal.valueOf(100)), "Expected an IllegalArgumentException to be thrown");
    assertThat(thrown.getMessage(), is("The buyer should be identified"));
  }

  @Test
  @Tag("Unit")
  void ctorNullPrice() {
    Bid bid = new Bid(BUYER_TEST, null);
    assertThat(bid.getPrice(), is(BigDecimal.ZERO));
  }

  @Test
  @Tag("Unit")
  void ctorNegativePrice() {
    Bid bid = new Bid(BUYER_TEST, BigDecimal.valueOf(-1));
    assertThat(bid.getPrice(), is(BigDecimal.ZERO));
  }

  @Test
  @Tag("Unit")
  void bidsAreNeverEquals() throws InterruptedException {
    Bid bid1 = new Bid(BUYER_TEST, BigDecimal.valueOf(100));
    Thread.sleep(1);// needs one millis
    Bid bid2 = new Bid(BUYER_TEST, BigDecimal.valueOf(100));
    assertNotEquals(bid1, bid2);
  }

}
