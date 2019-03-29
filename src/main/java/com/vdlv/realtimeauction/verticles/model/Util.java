package com.vdlv.realtimeauction.verticles.model;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public interface Util {
  /**
   * Represent a fictive Bid used when an auction didn't find any buyer.
   */
  Bid UnluckyAuctionBid = new Bid("This auction didn't find any buyer, we are sorry for that", BigDecimal.ZERO);

  static ZonedDateTime universalNow() {
    return ZonedDateTime.now(ZoneOffset.UTC);
  }
}
