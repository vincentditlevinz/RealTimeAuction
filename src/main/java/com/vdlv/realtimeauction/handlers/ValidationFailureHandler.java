package com.vdlv.realtimeauction.handlers;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface ValidationFailureHandler extends Handler<RoutingContext> {
  static ValidationFailureHandler create() {
    return new ValidationFailureHandlerImpl();
  }
}
