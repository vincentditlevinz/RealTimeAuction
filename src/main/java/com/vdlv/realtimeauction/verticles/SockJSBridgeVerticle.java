package com.vdlv.realtimeauction.verticles;

import com.vdlv.realtimeauction.model.Util;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 * SockJSBridgeVerticle aims to provide a bridge for server side event publication to ReactJS app through SockJS
 * (websocket if it can or various fallback protocol if not supported)
 *
 * @author vim
 */
public class SockJSBridgeVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(SockJSBridgeVerticle.class);

  @Override
  public void start() {
    Router router = Router.router(vertx);
    router.route("/eventbus/*").handler(eventBusHandler());
    vertx.createHttpServer().requestHandler(router).listen(7878);
  }

  private SockJSHandler eventBusHandler() {
    BridgeOptions options = new BridgeOptions()
      .addOutboundPermitted(new PermittedOptions().setAddress(Util.BidsTopic));
    return SockJSHandler.create(vertx).bridge(options, event -> {
      if (event.type() == BridgeEventType.SOCKET_CREATED) {
        logger.info("A socket was created listening to auction topics");
      }
      /*if (event.type() == BridgeEventType.PUBLISH || event.type() == BridgeEventType.SEND) {
          event.complete(false);// reject events from the client (client listen to events coming from server)
          return;
      }*/
      event.complete(true);
    });
  }
}

