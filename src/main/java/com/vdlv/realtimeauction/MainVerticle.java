package com.vdlv.realtimeauction;

import com.vdlv.realtimeauction.handlers.LoginHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.StaticHandler;
import xyz.jetdrone.vertx.spa.services.SPA;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() {
    final Router router = Router.router(vertx);

    AuthProvider shiroAuthProvider = ShiroAuth.create(
      vertx,
      new ShiroAuthOptions().setType(ShiroAuthRealmType.PROPERTIES).setConfig(new JsonObject())
    );

    router.route("/login").handler(LoginHandler.create(shiroAuthProvider));

    JWTAuthOptions authConfig = new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret"));

    JWTAuth authProvider = JWTAuth.create(vertx, authConfig);

    router.route("/api/*").handler(JWTAuthHandler.create(authProvider));

    router.route().handler(FaviconHandler.create("favicon.ico"));

    // mount the weather API
    router
      .get("/api/weather-forecast")
      .handler(WeatherAPI.get())
      .failureHandler(ErrorHandler.create("error-template.html", true));

    // will redirect to ng-serve while in development time
    router.route().handler(SPA.serve("react-clientapp", 7979));
    // Serve the static resources
    router.route().handler(StaticHandler.create());

    vertx.createHttpServer().requestHandler(router).listen(8080, res -> {
      if (res.failed()) {
        res.cause().printStackTrace();
      } else {
        System.out.println("Server listening at: http://localhost:8080/");
      }
    });
  }

  @Override
  public void stop() {
    // will stop any SPA running processes
    SPA.stop();
  }
}
