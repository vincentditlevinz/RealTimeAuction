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
import java.util.ArrayList;
import java.util.List;

import static com.vdlv.realtimeauction.repository.AuctionRepository.*;
import static org.exparity.hamcrest.date.ZonedDateTimeMatchers.after;
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
    assertThat(repo.findAuctions(0, 10).size(), is(1));
    context.completeNow();
  }

  @Test
  void simpleUpdateTest(Vertx vertx, VertxTestContext context) {
    AuctionRepository repo = new AuctionRepository(vertx);
    repo.upsertAuction(CARROTS_AND_POTATOES);
    assertThat(repo.findAuctions(0, 10).size(), is(1));

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
    assertThat(repo.findAuctions(0, 10).size(), is(1));

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
    repo.upsertAuction(new Auction("Other one", BigDecimal.valueOf(15)));
    assertThat(repo.findAuctions(0, 10).size(), is(3));
    assertThat(repo.findOpenAuctions(0, 10).size(), is(2));
    assertThat(repo.findClosedAuctions(0, 10).size(), is(1));
    context.completeNow();
  }

  @Test
  void findAuctionsPaginationAndSortingTest(Vertx vertx, VertxTestContext context) throws InterruptedException {
    AuctionRepository repo = new AuctionRepository(vertx);
    for (int i = 0; i < 10; i++) {
      repo.upsertAuction(new Auction("Carrots and potatoes", BigDecimal.valueOf(15)));
      Thread.sleep(100);
    }

    final List<Auction> openAuctions = repo.findOpenAuctions(0, 5);
    assertThat(openAuctions.size(), is(5));
    for (int i = 0; i < 4; i++) {
      assertThat(openAuctions.get(i).getEndingTime(), after(openAuctions.get(i + 1).getEndingTime()));
    }

    context.completeNow();
  }

  @Test
  void checkMaxItemPerPageGreaterThanListSizeTest() {
    assertThat(checkMax(3, 2), is(2));
    assertThat(checkMax(30, 20), is(20));
  }

  @Test
  void checkMaxItemPerPageGreaterThanListSizeAnd100Test() {
    assertThat(checkMax(101, 150), is(100));
    assertThat(checkMax(120, 80), is(80));
  }

  @Test
  void checkMaxItemPerPageIsNegativeTest() {
    assertThat(checkMax(-2, 2), is(2));
  }

  @Test
  void checkMaxItemPerPageIsNegativeAndListSizeHigherThan10Test() {
    assertThat(checkMax(-2, 20), is(10));
  }

  @Test
  void checkOffsetIsNegativeTest() {
    assertThat(checkOffset(-2, 20, 20), is(0));
  }

  @Test
  void checkOffsetGreaterThanListSizeTest() {
    assertThat(checkOffset(51, 20, 50), is(40));
  }

  @Test
  void checkOffsetEqualsListSizeTest() {
    assertThat(checkOffset(50, 20, 50), is(40));
  }

  @Test
  void checkOffsetLargerThanListSizeAndListSizeDividableByMaxTest() {
    assertThat(checkOffset(500, 10, 50), is(40));
  }

  @Test
  void checkOffsetLowerThanListSizeTest() {
    assertThat(checkOffset(10, 20, 50), is(10));
  }

  @Test
  void extractResultsHappyPathTest() throws InterruptedException {
    List<Auction> aList = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      aList.add(new Auction(("Name_" + i), BigDecimal.valueOf(15)));
      Thread.sleep(10);
    }

    List<Auction> extractedList = extractResults(aList, 0, 15);
    assertThat(extractedList.size(), is(15));
    assertThat(extractedList.get(0).getProduct(), is("Name_99"));
    assertThat(extractedList.get(14).getProduct(), is("Name_85"));
    extractedList = extractResults(aList, 15, 15);
    assertThat(extractedList.size(), is(15));
    assertThat(extractedList.get(0).getProduct(), is("Name_84"));
    assertThat(extractedList.get(14).getProduct(), is("Name_70"));
    extractedList = extractResults(aList, 30, 15);
    assertThat(extractedList.size(), is(15));
    assertThat(extractedList.get(0).getProduct(), is("Name_69"));
    assertThat(extractedList.get(14).getProduct(), is("Name_55"));
    extractedList = extractResults(aList, 45, 15);
    assertThat(extractedList.size(), is(15));
    assertThat(extractedList.get(0).getProduct(), is("Name_54"));
    assertThat(extractedList.get(14).getProduct(), is("Name_40"));
    extractedList = extractResults(aList, 60, 15);
    assertThat(extractedList.size(), is(15));
    assertThat(extractedList.get(0).getProduct(), is("Name_39"));
    assertThat(extractedList.get(14).getProduct(), is("Name_25"));
    extractedList = extractResults(aList, 75, 15);
    assertThat(extractedList.size(), is(15));
    assertThat(extractedList.get(0).getProduct(), is("Name_24"));
    assertThat(extractedList.get(14).getProduct(), is("Name_10"));
    extractedList = extractResults(aList, 90, 15);
    assertThat(extractedList.size(), is(10));
    assertThat(extractedList.get(0).getProduct(), is("Name_9"));
    assertThat(extractedList.get(9).getProduct(), is("Name_0"));

    // If we paginate ahead, still get the last page
    extractedList = extractResults(aList, 105, 15);
    assertThat(extractedList.size(), is(10));
    assertThat(extractedList.get(0).getProduct(), is("Name_9"));
    assertThat(extractedList.get(9).getProduct(), is("Name_0"));
  }

  @Test
  void extractResultsMaxNegativeTest() throws InterruptedException {
    List<Auction> aList = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      aList.add(new Auction(("Name_" + i), BigDecimal.valueOf(15)));
      Thread.sleep(10);
    }

    List<Auction> extractedList = extractResults(aList, 0, -1);
    assertThat(extractedList.size(), is(10));
    assertThat(extractedList.get(0).getProduct(), is("Name_99"));
    assertThat(extractedList.get(9).getProduct(), is("Name_90"));
  }

  @Test
  void extractResultsMaxTooHighOffsetTest() throws InterruptedException {
    List<Auction> aList = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      aList.add(new Auction(("Name_" + i), BigDecimal.valueOf(15)));
      Thread.sleep(10);
    }

    List<Auction> extractedList = extractResults(aList, 200, 10);
    assertThat(extractedList.size(), is(10));
    assertThat(extractedList.get(0).getProduct(), is("Name_9"));
    assertThat(extractedList.get(9).getProduct(), is("Name_0"));
  }
}
