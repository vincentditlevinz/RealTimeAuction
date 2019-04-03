package com.vdlv.realtimeauction.handlers;

import com.vdlv.realtimeauction.model.Auction;
import com.vdlv.realtimeauction.model.Bid;
import com.vdlv.realtimeauction.repository.AuctionRepository;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;

import java.math.BigDecimal;
import java.util.List;

public class AuctionHandler {
  private final AuctionRepository repository;
  private final static Logger logger = LoggerFactory.getLogger(AuctionHandler.class.getName());

  public AuctionHandler(AuctionRepository repository) {
    this.repository = repository;
  }

  public void handleGetAuctions(RoutingContext context) {
    RequestParameters params = context.get("parsedParameters");

    // Get parameters
    Boolean closed = null;
    if (params.queryParametersNames().contains("closed")) {// one must check for existence to avoid blocking
      closed = params.queryParameter("closed").getBoolean();
    }

    Integer offset = 0;
    if (params.queryParametersNames().contains("offset")) {// one must check for existence to avoid blocking
      offset = params.queryParameter("offset").getInteger();
    }
    Integer max = 10;
    if (params.queryParametersNames().contains("max")) {// one must check for existence to avoid blocking
      max = params.queryParameter("max").getInteger();
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Params: closed=" + closed + ", offset=" + offset + ", max=" + max);
      logger.debug("User: " + context.user().principal().getString("sub"));
    }

    List<Auction> result;
    if (closed == null) {
      result = repository.findAuctions(offset, max);
    } else if (closed) {
      result = repository.findClosedAuctions(offset, max);
    } else {
      result = repository.findOpenAuctions(offset, max);
    }
    if (logger.isDebugEnabled())
      logger.debug("Result:" + result);
    final JsonArray resp = new JsonArray();
    result.stream().forEach(item -> {
      resp.add(new JsonObject().
        put("id", item.getId()).
        put("product", item.getProduct()).
        put("price", item.getCurrentAuctionValue().doubleValue()).
        put("ending", item.getEndingTime().toString()));
    });
    context.response()
      .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.createOptimized("application/json"))
      .setStatusCode(200)
      .end(resp.encodePrettily());
  }

  public void handleBidForAuction(RoutingContext context) {
    RequestParameters params = context.get("parsedParameters");

    String auctionId = params.pathParameter("auctionId").getString();
    JsonObject bid = params.body().getJsonObject();

    repository.recordABid(auctionId, new Bid(context.user().principal().getString("sub"), BigDecimal.valueOf(bid.getDouble("price"))));

    context.response()
      .putHeader("content-type", "application/json")
      .setStatusCode(200)
      .end();
  }


}
