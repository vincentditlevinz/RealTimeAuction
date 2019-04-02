package com.vdlv.realtimeauction.repository;

import com.vdlv.realtimeauction.model.Auction;
import com.vdlv.realtimeauction.model.Bid;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.SharedData;

import java.util.*;

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
   * @param offset skip offset lines
   * @param max max item per page
   * @return the auctions that are still open
   */
  public List<Auction> findOpenAuctions(Integer offset, Integer max) {
    return extractResults(auctionStorage().values().stream().filter(Auction::isOpen).collect(toList()), offset, max);
  }

  /**
   * @param offset skip offset lines
   * @param max max item per page
   * @return the auctions that are closed
   */
  public List<Auction> findClosedAuctions(Integer offset, Integer max) {
    return extractResults(auctionStorage().values().stream().filter(Auction::isClosed).collect(toList()), offset, max);
  }

  /**
   * @param offset skip offset lines
   * @param max max item per page
   * @return all actions
   */
  public List<Auction> findAuctions(Integer offset, Integer max) {
    return extractResults(new ArrayList<>(auctionStorage().values()), offset, max);
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

  /**
   * Handle pagination
   *
   * @param result
   * @param offset
   * @param max
   * @return extracted results
   */
  static List<Auction> extractResults(List<Auction> result, Integer offset, Integer max) {
    int checkedMax = checkMax(max, result.size());
    int checkedOffset = checkOffset(offset, checkedMax, result.size());
    Collections.sort(result);
    int idEnd = checkedOffset + checkedMax;
    if (idEnd > result.size()) {
      idEnd = result.size();
    }
    return result.subList(checkedOffset, idEnd);
  }

  static int checkMax(int max, int listSize) {
    if (max <= 0) {
      return 10 > listSize ? listSize : 10;
    } else if (max > 100) {
      return 100 > listSize ? listSize : 100;
    } else {
      return max > listSize ? listSize : max;
    }
  }

  static int checkOffset(int offset, int checkedMax, int listSize) {
    if (offset <= 0) {
      return 0;
    } else {
      if (offset >= listSize) {
        int newOffset = Math.round(listSize / checkedMax) * checkedMax;
        if (newOffset == listSize) {
          newOffset -= checkedMax;
        }
        return newOffset;
      } else {
        return offset;
      }
    }
  }
}
