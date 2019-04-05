package com.vdlv.realtimeauction.verticles;

import com.vdlv.realtimeauction.handlers.AuctionHandler;
import com.vdlv.realtimeauction.handlers.LoginHandler;
import com.vdlv.realtimeauction.handlers.ValidationFailureHandler;
import com.vdlv.realtimeauction.model.Util;
import com.vdlv.realtimeauction.repository.AuctionRepository;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.api.validation.ParameterType;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import xyz.jetdrone.vertx.spa.services.SPA;

/**
 * FrontEndVerticle aims to deploy all endpoints (authentication and API) and static content (ReactJS app)
 *
 * @author vim
 */
public class FrontEndVerticle extends AbstractVerticle {
  private final static Logger logger = LoggerFactory.getLogger(FrontEndVerticle.class.getName());

  @Override
  public void start() {
    final Router router = Router.router(vertx);
    router.route().failureHandler(ErrorHandler.create());

    final Builder builder = new Builder(router);
    builder
      .setupAuthenticationEndpoint()
      .protectAPIEndpoints();

    AuctionHandler ah = new AuctionHandler(new AuctionRepository(vertx));
    HTTPRequestValidationHandler search = HTTPRequestValidationHandler.create()
      .addQueryParam("closed", ParameterType.BOOL, false)
      .addQueryParam("offset", ParameterType.INT, true)
      .addQueryParam("max", ParameterType.INT, true);


    router.get("/api/auctions")
      .handler(search)
      .handler(ah::handleGetAuctions)
      .failureHandler(ValidationFailureHandler.create());

    router.route("/api/bid/*").handler(BodyHandler.create());
    HTTPRequestValidationHandler patch = HTTPRequestValidationHandler.create()
      .addExpectedContentType("application/json")
      .addPathParam("auctionId", ParameterType.GENERIC_STRING);

    router.patch("/api/bid/:auctionId")
      .handler(patch)
      .handler(ah::handleBidForAuction)
      .failureHandler(ValidationFailureHandler.create());

    builder
      .setUpEventBusBridge()
      .publishSPAApplication()
      .setupDebuggingTools()
      .startVertxServer();
  }

  @Override
  public void stop() {
    // will stop any SPA running processes in DEV mode
    //SPA.stop();
  }

  /**
   * Helper class giving the intent
   */
  private class Builder {
    private Router router;

    Builder(Router router) {
      this.router = router;
    }

    Builder setupAuthenticationEndpoint() {
      AuthProvider shiroAuthProvider = ShiroAuth.create(
        vertx,
        new ShiroAuthOptions().setType(ShiroAuthRealmType.PROPERTIES).setConfig(new JsonObject())
      );
      router.route("/login").handler(LoginHandler.create(shiroAuthProvider)).failureHandler(context -> {
        if (logger.isDebugEnabled()) {
          logger.debug("Authentication failed");
        }
        context.response()
          .setStatusCode(401)
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML)
          .end("Unauthorized");
      });

      return this;
    }

    Builder protectAPIEndpoints() {
      JWTAuthOptions authConfig = new JWTAuthOptions()
        .setKeyStore(new KeyStoreOptions()
          .setType("jceks")
          .setPath("keystore.jceks")
          .setPassword("secret"));

      JWTAuth authProvider = JWTAuth.create(vertx, authConfig);

      router.route("/api/*").handler(JWTAuthHandler.create(authProvider)).failureHandler(context -> {
        if (logger.isDebugEnabled()) {
          logger.debug("JWT token is invalid or has expired.");
        }
        context.response()
          .setStatusCode(401)
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML)
          .end("Unauthorized");
      });
      return this;
    }

    Builder publishSPAApplication() {
      // Serve the static resources
      router.route().handler(StaticHandler.create());
      router.route().handler(FaviconHandler.create("favicon.ico"));
      return this;
    }

    Builder setupDebuggingTools() {
      // will redirect to ng-serve while in development time
      router.route().handler(SPA.serve("react-clientapp", 7979));
      return this;
    }

    Builder setUpEventBusBridge() {
      BridgeOptions options = new BridgeOptions()
        .addOutboundPermitted(new PermittedOptions().setAddress(Util.BidsTopic));
      SockJSHandler eventBusHandler = SockJSHandler.create(vertx).bridge(options, event -> {
        if (event.type() == BridgeEventType.SOCKET_CREATED) {
          logger.info("A socket was created listening to auction topics");
        }
      /*if (event.type() == BridgeEventType.PUBLISH || event.type() == BridgeEventType.SEND) {
          event.complete(false);// reject events from the client (client listen to events coming from server)
          return;
      }*/
        event.complete(true);
      });
      router.route("/eventbus/*").handler(eventBusHandler);
      return this;
    }

    void startVertxServer() {
      vertx.createHttpServer().requestHandler(router).listen(8080, res -> {
        if (res.failed()) {
          res.cause().printStackTrace();
        } else {
          logger.warn("Server listening at: http://localhost:8080/");
        }
      });
    }
  }
}
