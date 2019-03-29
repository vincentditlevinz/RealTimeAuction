package com.vdlv.realtimeauction.verticles;

import com.vdlv.realtimeauction.WeatherAPI;
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

/**
 * FrontEndVerticle aims to deploy all endpoints (authentication and API) and static content (ReactJS app)
 *
 * @author vim
 */
public class FrontEndVerticle extends AbstractVerticle {

  @Override
  public void start() {
    final Router router = Router.router(vertx);

    final Builder builder = new Builder(router);
    builder.setupAuthenticationEndpoint()
      .protectAPIEndpoints();


    // mount the weather API
    router
      .get("/api/weather-forecast")
      .handler(WeatherAPI.get())
      .failureHandler(ErrorHandler.create("error-template.html", true));

    builder
      .publishSPAApplication()
      .setupDebuggingTools()
      .startVertxServer();
  }

  @Override
  public void stop() {
    // will stop any SPA running processes in DEV mode
    SPA.stop();
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
      router.route("/login").handler(LoginHandler.create(shiroAuthProvider));
      return this;
    }

    Builder protectAPIEndpoints() {
      JWTAuthOptions authConfig = new JWTAuthOptions()
        .setKeyStore(new KeyStoreOptions()
          .setType("jceks")
          .setPath("keystore.jceks")
          .setPassword("secret"));

      JWTAuth authProvider = JWTAuth.create(vertx, authConfig);

      router.route("/api/*").handler(JWTAuthHandler.create(authProvider));
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

    void startVertxServer() {
      vertx.createHttpServer().requestHandler(router).listen(8080, res -> {
        if (res.failed()) {
          res.cause().printStackTrace();
        } else {
          System.out.println("Server listening at: http://localhost:8080/");
        }
      });
    }
  }
}
