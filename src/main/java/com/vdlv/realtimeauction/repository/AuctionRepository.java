package com.vdlv.realtimeauction.repository;

import com.vdlv.realtimeauction.model.Auction;
import com.vdlv.realtimeauction.model.Bid;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.SharedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * A simple Repository for persisting auctions. This repository use SharedData support of Vert.x, in a real world scenario, a database such as MongoDB or Redis
 * should be a better option.
 * The current implementation works only for a single node deployment (one JVM only), a distributable backend must be used otherwise.
 */
public class AuctionRepository {
  private final SharedData backend;

  public AuctionRepository(Vertx vertx) {
    this.backend = vertx.sharedData();
  }

  private Map<String, Auction> auctionStorage() {
    return backend.getLocalMap("auctions");
  }

  /**
   * @return the auctions that are still open
   */
  public List<Auction> findOpenAuctions() {
    return auctionStorage().values().stream().filter(Auction::isOpen).collect(toList());
  }

  /**
   * @return the auctions that are closed
   */
  public List<Auction> findClosedAuctions() {
    return auctionStorage().values().stream().filter(Auction::isOpen).collect(toList());
  }

  /**
   * @return all actions
   */
  public List<Auction> findAuctions() {
    return new ArrayList<>(auctionStorage().values());
  }

  /**
   * @param id the id of the auction
   * @return the auction if any
   */
  public Optional<Auction> findAuctionById(String id) {
    return Optional.ofNullable(auctionStorage().get(id));
  }

  /**
   * Insert or override the auction (full replacement, the last win)
   *
   * @param auction
   * @return
   */
  public Auction upsertAuction(Auction auction) {
    return auctionStorage().put(auction.getId(), auction);
  }

  /**
   * Try to associate a bid to an auction
   *
   * @param id  auction Id
   * @param bid the bid
   * @return true if the bid is recorded. False is returned if the bid is not acceptable (see {@link Auction#addBid(Bid))} or if the auction does not exists
   */
  public boolean recordABid(String id, Bid bid) {
    Optional<Auction> result = findAuctionById(id);
    if (result.isPresent()) {
      final Auction auction = result.get();
      final boolean returnValue = auction.addBid(bid);
      upsertAuction(auction);
      return returnValue;// is correctly added
    } else {
      return false;// the auction does not exists
    }
  }
}
