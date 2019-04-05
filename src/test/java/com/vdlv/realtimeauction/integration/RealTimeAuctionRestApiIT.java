package com.vdlv.realtimeauction.integration;

import io.github.glytching.junit.extension.system.SystemProperty;
import io.github.glytching.junit.extension.system.SystemPropertyExtension;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;


@ExtendWith({VertxExtension.class, SystemPropertyExtension.class})
@SystemProperty(name = "vertx.environment", value = "INT")
class RealTimeAuctionRestApiIT {
  private final static Logger logger = LoggerFactory.getLogger(RealTimeAuctionRestApiIT.class.getName());

  @BeforeAll
  public static void configureRestAssured() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = Integer.getInteger("http.port", 8080);
    logger.warn("Restassured is executing on " + RestAssured.baseURI + ":" + RestAssured.port);
    wait(500);
  }

  /**
   * Pause current thread
   *
   * @param millis pause duration in millis
   */
  private static void wait(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @AfterAll
  public static void unconfigureRestAssured() {
    RestAssured.reset();
  }

  /**
   * Use Rest-Assured for testing authentication and getting the JWT token (I need an integration test library for that
   * not vertx unit test framework)
   *
   * @param username user name
   * @param password paswword
   * @param success  if authentication should be successful or not
   * @return the JWT token if successful, an empty string if not
   */
  String authenticate(String username, String password, boolean success) {
    JsonObject body = new JsonObject().put("username", username).put("password", password);
    Response response =
      given().
        body(body.encodePrettily()).
        with().
        contentType(ContentType.JSON).
        when().
        post("/login").
        then().
        statusCode(success ? 200 : 401).
        extract().
        response();
    return success ? response.body().jsonPath().getString("token") : "";
  }

  @Test
  void authenticationSuccessTest() {
    authenticate("martin", "test123", true);
  }

  @Test
  void authenticationWrongPasswordTest() {
    authenticate("martin", "password", false);
  }

  @Test
  void authenticationWrongLoginTest() {
    authenticate("Giuseppe", "test123", false);
  }

  @Test
  void unknownPath() {
    given().
      when().
      get("/dummy").
      then().
      statusCode(404);
  }
}
