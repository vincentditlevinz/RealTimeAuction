package com.vdlv.realtimeauction.model;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public interface Util {
  /**
   * Represent a fictive Bid used when an auction didn't find any buyer.
   */
  Bid UnluckyAuctionBid = new Bid("This auction didn't find any buyer, we are sorry for that", BigDecimal.ZERO);

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
}
