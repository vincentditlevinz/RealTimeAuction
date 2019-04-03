package com.vdlv.realtimeauction.verticles;

import com.vdlv.realtimeauction.repository.AuctionRepository;
import io.github.glytching.junit.extension.system.SystemProperty;
import io.github.glytching.junit.extension.system.SystemPropertyExtension;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@ExtendWith({VertxExtension.class, SystemPropertyExtension.class})
@SystemProperty(name = "vertx.environment", value = "JUNIT")
class FrontEndVerticleTest {
  private final static Logger logger = LoggerFactory.getLogger(FrontEndVerticleTest.class.getName());

  @BeforeAll
  public static void configureRestAssured() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = Integer.getInteger("http.port", 8080);
  }

  @AfterAll
  public static void unconfigureRestAssured() {
    RestAssured.reset();
  }

  @BeforeEach
  void init(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new FrontEndVerticle(), testContext.succeeding(ar -> {
      wait(50);// ensure server is really really started (we had sometimes connection refused messages)
      testContext.completeNow();
    }));
    AuctionManagementVerticle.initializeAuctions(new AuctionRepository(vertx));
  }

  /**
   * Pause current thread
   *
   * @param millis pause duration in millis
   */
  private void wait(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Test
  void getAuctions_ExpiredTokenTest(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    webClient.get(8080, "localhost", "/api/auctions")
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
    WebClient webClient = WebClient.create(vertx);
    webClient.get(8080, "localhost", "/api/auctions")
      .addQueryParam("offset", "0")
      .addQueryParam("max", "10")
      .addQueryParam("closed", "false")
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(401));
        assertThat(response.statusMessage(), is("Unauthorized"));
        webClient.close();
        testContext.completeNow();
      })));
  }

  @Test
  void authentication_WrongCredentialsTest(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    webClient.get(8080, "localhost", "/login")
      .sendJsonObject(new JsonObject().put("username", "martin").put("password", "wrongPassword"), testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(401));
        assertThat(response.statusMessage(), is("Unauthorized"));
        testContext.completeNow();
      })));
  }

  @Test
  void authentication_ValidCredentialsTest(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    webClient.post(8080, "localhost", "/login")
      .sendJsonObject(new JsonObject().put("username", "martin").put("password", "test123"), testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(200));
        JsonObject result = response.bodyAsJsonObject();
        assertThat(result.getBoolean("authenticated"), is(true));
        assertThat(result.getString("token"), not(isEmptyString()));
        testContext.completeNow();
      })));
  }

  @Test
  void getAllAuctions(Vertx vertx, VertxTestContext testContext) {
    String token = authenticate("martin", "test123");
    WebClient webClient = WebClient.create(vertx);
    webClient.get(8080, "localhost", "/api/auctions")
      .addQueryParam("offset", "0")
      .addQueryParam("max", "10")
      .bearerTokenAuthentication(token)
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(200));
        JsonArray result = response.bodyAsJsonArray();
        assertThat(result.size(), is(4));
        logger.info("Result from http request:" + result);
        testContext.completeNow();
      })));
  }

  @Test
  void getOpenAuctions(Vertx vertx, VertxTestContext testContext) {
    String token = authenticate("martin", "test123");
    WebClient webClient = WebClient.create(vertx);
    webClient.get(8080, "localhost", "/api/auctions")
      .addQueryParam("offset", "0")
      .addQueryParam("max", "10")
      .addQueryParam("closed", "false")
      .bearerTokenAuthentication(token)
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(200));
        JsonArray result = response.bodyAsJsonArray();
        assertThat(result.size(), is(4));
        testContext.completeNow();
      })));
  }

  @Test
  void getClosedAuctions(Vertx vertx, VertxTestContext testContext) {
    String token = authenticate("martin", "test123");
    WebClient webClient = WebClient.create(vertx);
    webClient.get(8080, "localhost", "/api/auctions")
      .addQueryParam("offset", "0")
      .addQueryParam("max", "10")
      .addQueryParam("closed", "true")
      .bearerTokenAuthentication(token)
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(200));
        JsonArray result = response.bodyAsJsonArray();
        assertThat(result.isEmpty(), is(true));
        testContext.completeNow();
      })));
  }

  @Test
  void bidForAnAuction(Vertx vertx, VertxTestContext testContext) {
    String token = authenticate("martin", "test123");
    JsonArray auctions = auctions(false, token);
    JsonObject auction = auctions.getJsonObject(0);

    WebClient webClient = WebClient.create(vertx);
    webClient.patch(8080, "localhost", "/api/bid/" + auction.getString("id"))
      .bearerTokenAuthentication(token)
      .sendJsonObject(new JsonObject().put("price", 10000.0), testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode(), is(200));
        JsonObject body = response.bodyAsJsonObject();
        assertThat(body.getDouble("price"), is(10000.0));
        assertThat(body.getString("buyer"), is("martin"));
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
        statusCode(200).
        body("authenticated", equalTo(true)).
        extract().
        response();
    return response.body().jsonPath().getString("token");
  }

  JsonArray auctions(boolean closed, String token) {
    Response response =
      given().
        queryParam("offset", "0").
        queryParam("max", "10").
        queryParam("closed", closed).
        auth().oauth2(token).
        with().
        contentType(ContentType.JSON).
        when().
        get("/api/auctions").
        then().
        statusCode(200).
        extract().
        response();
    JsonArray result = new JsonArray();
    response.body().jsonPath().getList("").forEach(item -> {
      JsonObject itemAsJson = new JsonObject((Map) item);
      result.add(itemAsJson);
    });
    return result;
  }
}
