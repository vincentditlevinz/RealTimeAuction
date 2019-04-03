package com.vdlv.realtimeauction.handlers;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.validation.ValidationException;

class ValidationFailureHandlerImpl implements ValidationFailureHandler {

  @Override
  public void handle(RoutingContext context) {
    Throwable failure = context.failure();
    if (failure instanceof ValidationException) {
      ValidationException ve = (ValidationException) failure;
      JsonObject message = new JsonObject().put("type", "ValidationException").
        put("param", ve.parameterName()).
        put("value", ve.value()).
        put("message", ve.getMessage());
      context.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.createOptimized("application/json"))
        .setStatusCode(422)
        .end(message.encodePrettily());
    }
  }
}
