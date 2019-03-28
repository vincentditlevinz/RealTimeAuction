package com.vdlv.realtimeauction.handlers;

import io.vertx.core.Handler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;

public interface LoginHandler extends Handler<RoutingContext> {
  static LoginHandler create(AuthProvider authProvider) {
    return new LoginHandlerImpl(authProvider);
  }
}
