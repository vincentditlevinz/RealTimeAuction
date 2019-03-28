package com.vdlv.realtimeauction.handlers;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.RoutingContext;

public class LoginHandlerImpl implements LoginHandler {
  private final AuthProvider authProvider;

  LoginHandlerImpl(AuthProvider authProvider) {
    this.authProvider = authProvider;
  }

  @Override
  public void handle(RoutingContext context) {
    context.request().bodyHandler(bodyHandler -> {
      final JsonObject authInfo = bodyHandler.toJsonObject();
      authProvider.authenticate(authInfo, res -> {
        if (res.succeeded()) {
          JWTAuthOptions config = new JWTAuthOptions()
            .setKeyStore(new KeyStoreOptions()
              .setPath("keystore.jceks")
              .setPassword("secret"));

          JWTAuth provider = JWTAuth.create(context.vertx(), config);
          final String token = provider.generateToken(new JsonObject().put("sub", authInfo.getValue("username")), new JWTOptions().setExpiresInMinutes(60));
          JsonObject result = new JsonObject().put("token", token).put("authenticated", true);
          context.response().end(result.toString());
        } else {
          context.fail(401);
        }
      });
    });


  }
}
