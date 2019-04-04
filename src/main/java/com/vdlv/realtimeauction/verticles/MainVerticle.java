package com.vdlv.realtimeauction.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * MainVerticle start all dependent verticles of the application
 *
 * @author vim
 */
public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start() {
    vertx.deployVerticle(AuctionManagementVerticle.class.getName(), res -> {
      if (res.succeeded()) {
        logger.info("AuctionManagementVerticle deployment id is: " + res.result());
      } else {
        logger.error("AuctionManagementVerticle deployment failed!");
      }
    });
    vertx.deployVerticle(FrontEndVerticle.class.getName(), res -> {
      if (res.succeeded()) {
        logger.info("FrontEndVerticle deployment id is: " + res.result());
      } else {
        logger.error("FrontEndVerticle deployment failed!");
      }
    });
    vertx.deployVerticle(SockJSBridgeVerticle.class.getName(), res -> {
      if (res.succeeded()) {
        logger.info("SockJSBridgeVerticle deployment id is: " + res.result());
      } else {
        logger.error("SockJSBridgeVerticle deployment failed!");
      }
    });
  }
}

