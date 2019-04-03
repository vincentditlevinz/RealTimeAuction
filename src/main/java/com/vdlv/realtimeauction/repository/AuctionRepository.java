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
   * @param auction the auction to insert or override
   * @return the auction (might be useful with certain types of repository that generates ids)
   */
  public Auction upsertAuction(Auction auction) {
    auctionStorage().put(auction.getId(), auction);
    return auction;
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
   * Handle pagination (a sort is done during the process)
   * @param all the complete list of results
   * @param offset the offset from to start extraction
   * @param max the number of items to extract
   * @return extracted results
   */
  static List<Auction> extractResults(List<Auction> all, Integer offset, Integer max) {
    int checkedMax = checkMax(max, all.size());
    int checkedOffset = checkOffset(offset, checkedMax, all.size());
    Collections.sort(all);
    int idEnd = checkedOffset + checkedMax;
    if (idEnd > all.size()) {
      idEnd = all.size();
    }
    return all.subList(checkedOffset, idEnd);
  }

  /**
   * Evaluates the max value against the list size.
   *
   * @param max      max item per page
   * @param listSize the size of the whole result set
   * @return the max value reevaluated
   */
  static int checkMax(int max, int listSize) {
    if (max <= 0) {
      return 10 > listSize ? listSize : 10;
    } else if (max > 100) {
      return 100 > listSize ? listSize : 100;
    } else {
      return max > listSize ? listSize : max;
    }
  }

  /**
   * @param offset the offset from which starting the extraction
   * @param checkedMax max item per page
   * @param listSize the size of the whole result set
   * @return the offset value reevaluated
   */
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
