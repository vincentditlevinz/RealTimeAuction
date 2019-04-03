package com.vdlv.realtimeauction.handlers;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.RoutingContext;

public class LoginHandlerImpl implements LoginHandler {
  private final AuthProvider authProvider;
  private final static Logger logger = LoggerFactory.getLogger(LoginHandler.class.getName());

  LoginHandlerImpl(AuthProvider authProvider) {
    this.authProvider = authProvider;
  }

  @Override
  public void handle(RoutingContext context) {
    context.request().bodyHandler(bodyHandler -> {
      final JsonObject authInfo = bodyHandler.toJsonObject();
      if (logger.isDebugEnabled()) {
        logger.debug("Authenticating " + authInfo.getValue("username") + "...");
      }
      authProvider.authenticate(authInfo, res -> {
        if (res.succeeded()) {
          if (logger.isDebugEnabled()) {
            logger.debug("User " + authInfo.getValue("username") + " has been authenticated.");
          }
          JWTAuthOptions config = new JWTAuthOptions()
            .setKeyStore(new KeyStoreOptions()
              .setPath("keystore.jceks")
              .setPassword("secret"));

          JWTAuth provider = JWTAuth.create(context.vertx(), config);
          final String token = provider.generateToken(new JsonObject().put("sub", authInfo.getValue("username")), new JWTOptions().setExpiresInMinutes(60));
          JsonObject result = new JsonObject().put("token", token).put("authenticated", true);
          context.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.createOptimized("application/json"))
            .end(result.encodePrettily());
        } else {
          if (logger.isDebugEnabled()) {
            logger.debug("User " + authInfo.getValue("username") + " authentication failed.");
          }
          context.fail(401);
        }
      });
    });


  }
}
