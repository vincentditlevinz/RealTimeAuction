package com.vdlv.realtimeauction.model;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static java.math.RoundingMode.CEILING;

public interface Util {
  BigDecimal MINUS_ONE = BigDecimal.valueOf(-1).setScale(2, CEILING);
  BigDecimal ZERO = BigDecimal.ZERO.setScale(2, CEILING);
  BigDecimal TEN = BigDecimal.valueOf(10).setScale(2, CEILING);
  BigDecimal FIFTEEN = BigDecimal.valueOf(15).setScale(2, CEILING);
  BigDecimal TWENTY = BigDecimal.valueOf(20).setScale(2, CEILING);
  BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100).setScale(2, CEILING);
  BigDecimal TWO_HUNDRED = BigDecimal.valueOf(200).setScale(2, CEILING);
  BigDecimal THREE_HUNDRED = BigDecimal.valueOf(300).setScale(2, CEILING);
  BigDecimal THREE_HUNDRED_ONE = BigDecimal.valueOf(301).setScale(2, CEILING);
  BigDecimal FOUR_HUNDRED = BigDecimal.valueOf(400).setScale(2, CEILING);
  BigDecimal FIVE_HUNDRED = BigDecimal.valueOf(500).setScale(2, CEILING);
  BigDecimal THOUSAND = BigDecimal.valueOf(1000).setScale(2, CEILING);
  BigDecimal THOUSAND_HALF = BigDecimal.valueOf(1500).setScale(2, CEILING);
  BigDecimal TWO_THOUSAND = BigDecimal.valueOf(2000).setScale(2, CEILING);


  /**
   * Represent a fictive Bid used when an auction didn't find any buyer.
   */
  Bid UnluckyAuctionBid = new Bid("May be you!", ZERO);

  /**
   * One should worry about time with an auction system that may run world wild
   *
   * @return 'now' according to UTC time zone offset
   */
  static ZonedDateTime universalNow() {
    return ZonedDateTime.now(ZoneOffset.UTC);
  }

  static long auctionValidityInMinutes() {
    return 5;
  }

  String BidsTopic = "bids";
}
