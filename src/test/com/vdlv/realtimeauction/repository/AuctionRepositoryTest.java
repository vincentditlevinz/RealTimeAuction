package com.vdlv.realtimeauction.repository;

import com.vdlv.realtimeauction.model.Auction;
import com.vdlv.realtimeauction.model.Bid;
import com.vdlv.realtimeauction.model.Util;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(VertxExtension.class)
class AuctionRepositoryTest {

  private static final Auction CARROTS_AND_POTATOES = new Auction("Carrots and potatoes", BigDecimal.valueOf(15));
  private final static String BUYER = "John Doe";

  @Test
  void simpleInsertTest(Vertx vertx, VertxTestContext context) {
    AuctionRepository repo = new AuctionRepository(vertx);
    repo.upsertAuction(CARROTS_AND_POTATOES);
    assertThat(repo.findAuctions().size(), is(1));
    context.completeNow();
  }

  @Test
  void simpleUpdateTest(Vertx vertx, VertxTestContext context) {
    AuctionRepository repo = new AuctionRepository(vertx);
    repo.upsertAuction(CARROTS_AND_POTATOES);
    assertThat(repo.findAuctions().size(), is(1));

    Auction auction = repo.findAuctionById(CARROTS_AND_POTATOES.getId()).get();
    assertThat(auction.getProduct(), is("Carrots and potatoes"));
    auction.addBid(new Bid(BUYER, BigDecimal.valueOf(20)));
    repo.upsertAuction(auction);

    auction = repo.findAuctionById(CARROTS_AND_POTATOES.getId()).get();
    assertThat(auction.getCurrentAuctionValue(), is(BigDecimal.valueOf(20)));
    context.completeNow();
  }

  @Test
  void recordABidTest(Vertx vertx, VertxTestContext context) {
    AuctionRepository repo = new AuctionRepository(vertx);
    repo.upsertAuction(CARROTS_AND_POTATOES);
    assertThat(repo.findAuctions().size(), is(1));

    repo.recordABid(CARROTS_AND_POTATOES.getId(), new Bid(BUYER, BigDecimal.valueOf(20)));

    Auction auction = repo.findAuctionById(CARROTS_AND_POTATOES.getId()).get();
    assertThat(auction.getCurrentAuctionValue(), is(BigDecimal.valueOf(20)));
    context.completeNow();
  }

  @Test
  void findAuctionsTest(Vertx vertx, VertxTestContext context) throws InterruptedException {
    AuctionRepository repo = new AuctionRepository(vertx);
    Auction shortTermAuction = new Auction("Mercedes Class A", BigDecimal.valueOf(1500), Util.universalNow().plus(10, ChronoUnit.MILLIS));
    shortTermAuction.addBid(new Bid(BUYER, BigDecimal.valueOf(2000)));
    repo.upsertAuction(shortTermAuction);
    Thread.sleep(10);
    repo.upsertAuction(CARROTS_AND_POTATOES);
    assertThat(repo.findAuctions().size(), is(2));
    assertThat(repo.findOpenAuctions().size(), is(1));
    assertThat(repo.findClosedAuctions().size(), is(1));
    context.completeNow();
  }
}
