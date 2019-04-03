package com.vdlv.realtimeauction.verticles;

import io.github.glytching.junit.extension.system.SystemProperty;
import io.github.glytching.junit.extension.system.SystemPropertyExtension;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@ExtendWith({VertxExtension.class, SystemPropertyExtension.class})
@SystemProperty(name = "vertx.environment", value = "JUNIT")
class FrontEndVerticleTest {

  @BeforeEach
  void init(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new FrontEndVerticle(), testContext.completing());
  }

  @Test
  void getAuctions_ExpiredTokenTest(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(8080, "localhost", "/api/auctions")
      .addQueryParam("offset", "0")
      .addQueryParam("max", "10")
      .addQueryParam("closed", "false")
      .bearerTokenAuthentication("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYXJ0aW4iLCJpYXQiOjE1NTQyMTg4OTcsImV4cCI6MTU1NDIyMjQ5N30.UOnu9qf6Gi6SB5P2hg4-A6xdhLeLc9VU6ycMPgEo")
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(401));
        assertThat(response.statusMessage(), is("Unauthorized"));
        testContext.completeNow();
      })));
  }

  @Test
  void getAuctions_NoTokenTest(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(8080, "localhost", "/api/auctions")
      .addQueryParam("offset", "0")
      .addQueryParam("max", "10")
      .addQueryParam("closed", "false")
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(401));
        assertThat(response.statusMessage(), is("Unauthorized"));
        testContext.completeNow();
      })));
  }

  @Test
  void authentication_WrongCredentialsTest(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(8080, "localhost", "/login")
      .sendJsonObject(new JsonObject().put("username", "martin").put("password", "wrongPassword"), testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(401));
        assertThat(response.statusMessage(), is("Unauthorized"));
        testContext.completeNow();
      })));
  }

  @Test
  void authentication_ValidCredentialsTest(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.post(8080, "localhost", "/login")
      .sendJsonObject(new JsonObject().put("username", "martin").put("password", "test123"), testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(200));
        JsonObject result = response.bodyAsJsonObject();
        assertThat(result.getBoolean("authenticated"), is(true));
        assertThat(result.getString("token"), not(isEmptyString()));
        testContext.completeNow();
      })));
  }

  @Test
  void getAuctions(Vertx vertx, VertxTestContext testContext) {
    String token = authenticate("martin", "test123");
    System.err.println(token);
    WebClient client = WebClient.create(vertx);
    client.get(8080, "localhost", "/api/auctions")
      .addQueryParam("offset", "0")
      .addQueryParam("max", "10")
      .addQueryParam("closed", "false")
      .bearerTokenAuthentication(token)
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(200));
        testContext.completeNow();
      })));
  }

  /**
   * Use Rest-Assured for testing authentication and getting the JWT token (I need an integration test library for that
   * not vertx unit test framework)
   *
   * @param username user name
   * @param password paswword
   * @return the JWT token if successful
   */
  String authenticate(String username, String password) {
    JsonObject body = new JsonObject().put("username", username).put("password", password);
    Response response =
      given().
        body(body.encodePrettily()).
        with().
        contentType(ContentType.JSON).
        when().
        post("/login").
        then().
        body("authenticated", equalTo(true)).
        extract().
        response();
    return response.body().jsonPath().getString("token");
  }
}
