package com.vdlv.realtimeauction.handlers;

import com.vdlv.realtimeauction.model.Auction;
import com.vdlv.realtimeauction.repository.AuctionRepository;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;

import java.util.List;

public class AuctionHandler {
  private final AuctionRepository repository;

  public AuctionHandler(AuctionRepository repository) {
    this.repository = repository;
  }

  public void handleGetAuctions(RoutingContext context) {
    RequestParameters params = context.get("parsedParameters");

    // Get parameters
    Boolean closed = params.queryParameter("closed").getBoolean();
    Integer offset = params.queryParameter("offset").getInteger();
    Integer max = params.queryParameter("max").getInteger();

    List<Auction> result;
    if (closed == null) {
      result = repository.findAuctions(offset, max);
    } else if (closed) {
      result = repository.findClosedAuctions(offset, max);
    } else {
      result = repository.findOpenAuctions(offset, max);
    }

    context.response()
      .putHeader("content-type", "application/json")
      .setStatusCode(200)
      .end(Json.encodePrettily(result));
  }


}
