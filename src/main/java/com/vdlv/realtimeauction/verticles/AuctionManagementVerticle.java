package com.vdlv.realtimeauction.verticles;

import com.vdlv.realtimeauction.model.Auction;
import com.vdlv.realtimeauction.model.Util;
import com.vdlv.realtimeauction.repository.AuctionRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.asList;

/**
 * AuctionManagementVerticle put automatically some auctions in the repository
 *
 * @author vim
 */
public class AuctionManagementVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(AuctionManagementVerticle.class);
  private final static List<String> catalog = asList("Google TV", "Honda Civic 1989", "100 pairs of socks", "Potatoes");
  private final static List<BigDecimal> prices = asList(valueOf(1000), valueOf(2000), valueOf(100), valueOf(20));

  @Override
  public void start() {
    AuctionRepository repository = new AuctionRepository(vertx);
    initializeAuctions(repository);
    vertx.setPeriodic(Util.auctionValidityInMinutes() * 30000, id -> {
      int i = ThreadLocalRandom.current().nextInt(0, 4);
      Auction auction = repository.upsertAuction(new Auction(catalog.get(i), prices.get(i)));
      logger.info("Inserting auction: " + auction);
    });
  }

  public static void initializeAuctions(AuctionRepository repository) {
    for (int i = 0; i < 4; i++) {
      Auction auction = repository.upsertAuction(new Auction(catalog.get(i), prices.get(i)));
      logger.info("Inserting auction: " + auction);
    }
  }
}

