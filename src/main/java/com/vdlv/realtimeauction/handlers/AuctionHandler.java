package com.vdlv.realtimeauction.handlers;

import com.vdlv.realtimeauction.model.Auction;
import com.vdlv.realtimeauction.model.Bid;
import com.vdlv.realtimeauction.model.Util;
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

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpHeaders.createOptimized;

public class AuctionHandler {
  private final AuctionRepository repository;
  private final static Logger logger = LoggerFactory.getLogger(AuctionHandler.class.getName());

  public AuctionHandler(AuctionRepository repository) {
    this.repository = repository;
  }

  /**
   * Retrieves auctions stored in the backend according to http parameters (closed, offset and max)
   * The result is sent as a Json array of Json objects containing the auction id, product, price and ending time.
   *
   * @param context the routing context
   */
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
    result.forEach(item -> resp.add(convert(item)));
    context.response()
      .putHeader(CONTENT_TYPE, createOptimized("application/json"))
      .setStatusCode(200)
      .end(resp.encode());
  }

  /**
   * Record a bid for an auction. The auction is identified by a path parameter, the bid price is specified through a Json
   * request body and the user is identified thanks to the JWT token.
   * @param context the routing context
   */
  public void handleBidForAuction(RoutingContext context) {
    RequestParameters params = context.get("parsedParameters");

    String auctionId = params.pathParameter("auctionId").getString();
    JsonObject bid = context.getBodyAsJson();

    if (logger.isDebugEnabled()) {
      logger.debug("Params: auctionId=" + auctionId + ", price=" + bid.getDouble("price"));
      logger.debug("User: " + context.user().principal().getString("sub"));
    }

    boolean ok = repository.recordABid(auctionId, new Bid(context.user().principal().getString("sub"), BigDecimal.valueOf(bid.getDouble("price"))));
    if (ok) {
      Auction updatedAuction = repository.findAuctionById(auctionId).get();
      // Publish to the event bus for web socket integration
      context.vertx().eventBus().publish(Util.BidsTopic, convert(updatedAuction).encode());
      context.response()
        .putHeader(CONTENT_TYPE, createOptimized("application/json"))
        .setStatusCode(200)
        .end(convert(updatedAuction).encode());
    } else {
      JsonObject message = new JsonObject().put("type", "BidException");
      Auction targetedAuction = repository.findAuctionById(auctionId).get();

      if (targetedAuction.isClosed()) {
        message.put("message", "Sorry, the auction is closed for this product");
      } else {
        message.put("message", "Sorry, your offer is below the current product price");
      }
      context.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, createOptimized("application/json"))
        .setStatusCode(422)
        .end(message.encodePrettily());
    }
  }

  /**
   * Convert an auction to a JsonObject
   *
   * @param auction the auction to convert
   * @return the JsonObject
   */
  private static JsonObject convert(Auction auction) {
    return new JsonObject().
      put("id", auction.getId()).
      put("product", auction.getProduct()).
      put("price", auction.getCurrentAuctionValue().doubleValue()).
      put("ending", auction.getEndingTime().toString()).
      put("buyer", auction.getCurrentBuyer());
  }


}
