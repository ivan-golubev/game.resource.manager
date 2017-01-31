package net.ivango.game.resourcemanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import net.ivango.game.resourcemanager.config.HttpSessionConfig;
import net.ivango.game.resourcemanager.entities.GameResource;
import net.ivango.game.resourcemanager.entities.ResourceId;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

import static io.restassured.RestAssured.given;

public class ResourceManagerTest {

    public static Logger logger = LoggerFactory.getLogger(ResourceManagerTest.class);
    private static ConfigurableApplicationContext springBootApp;

    @BeforeClass
    public static void init() {
        springBootApp = new SpringApplication(StartServer.class).run();
        RestAssured.port = 8080;
    }

    @AfterClass
    public static void shutdown() { springBootApp.close(); }

    private String loginWithValidCredentials(boolean alice) {
        String login = alice ? "alice" : "bob";
        String pass = alice ? "03a04a47-2694-4289-9d2b-b0ab0b3ab391" : "5868f7ec-ef69-4943-a46c-7e4484b9940e";

        return
        given()
                .auth().preemptive().basic(login, pass)
                //                .log().everything()
        .when()
                .post("/auth/login")
        .then()
                .statusCode(200)
                .extract().header(HttpSessionConfig.SESSION_HEADER);
    }

    @Test
    public void testAuth() {
        logger.info("Authenticating using valid credentials...");
        String sessionToken = loginWithValidCredentials(true);
        Assert.assertNotNull(sessionToken);

        logger.info("Authenticating using invalid credentials...");
        String sessionToken2 =
                given()
                        .auth().preemptive().basic("alice", "wrong-password")
                .when()
                        .post("/auth/login")
                .then()
                        .statusCode(401)
                        .extract().header(HttpSessionConfig.SESSION_HEADER);

        Assert.assertNull(sessionToken2);
    }

    @Test
    public void testUnauthenticatedAccess() throws IOException {
        logger.info("Trying to access API without auth token...");

        logger.info("Creating a resource...");
        String resourceJSON = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("SampleResource.json"),
                "UTF-8"
        );

        String game = "doom";
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(resourceJSON)
        .when()
                .post("/resources?game=" + game)
        .then().
                statusCode(401);

        logger.info("Updating a resource...");
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(resourceJSON)
        .when()
                .put("/resources/42?game=" + game)
        .then().
                statusCode(401);

        logger.info("Deleting a resource...");
        given()
        .when()
                .delete("/resources/42?game=" + game)
        .then().
                statusCode(401);
    }

    @Test
    public void testResourceCreateUpdateDelete()  throws IOException {
        logger.info("Testing resource creation...");
        String sessionAlice = loginWithValidCredentials(true);
        String srcJSONFromFile = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("SampleResource.json"),
                "UTF-8"
        );

        String game = "doom";
        logger.info("Creating a resource as Alice...");
        String responseJSON =
            given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .header(new Header(HttpSessionConfig.SESSION_HEADER, sessionAlice))
                    .body(srcJSONFromFile)
            .when()
                    .post("/resources?game=" + game)
            .then().
                    statusCode(200)
                    .extract().response().asString();

        ObjectMapper jacksonMapper = new ObjectMapper();
        ResourceId resourceId = jacksonMapper.readValue(responseJSON, ResourceId.class);

        logger.info("Fetching the resource...");
        responseJSON =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .header(new Header(HttpSessionConfig.SESSION_HEADER, sessionAlice))
                        .body(srcJSONFromFile)
                .when()
                        .get("/resources/" + resourceId.getResourceId() + "?game=" + game)
                        .then().
                statusCode(200)
                        .extract().response().asString();

        GameResource gameResource = jacksonMapper.readValue(responseJSON, GameResource.class);
        Assert.assertEquals(resourceId.getResourceId(), gameResource.getId());
        Assert.assertEquals("alice", gameResource.getOwner());

        logger.info("Updating the resource as Bob...");
        String sessionBob = loginWithValidCredentials(false);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header(new Header(HttpSessionConfig.SESSION_HEADER, sessionBob))
                .body(srcJSONFromFile)
        .when()
                .put("/resources/" + resourceId.getResourceId() + "?game=" + game)
        .then().
                statusCode(403); // Bob is not authorized to modify the resource created by Alice !

        logger.info("Updating the resource as Alice...");
        gameResource.setName("New name");
        gameResource.setStatus("offline");
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header(new Header(HttpSessionConfig.SESSION_HEADER, sessionAlice))
                .body(jacksonMapper.writeValueAsString(gameResource))
        .when()
                .put("/resources/" + resourceId.getResourceId() + "?game=" + game)
        .then().
                statusCode(200);

        logger.info("Verifying that Alice has updated the resource...");
        responseJSON =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .header(new Header(HttpSessionConfig.SESSION_HEADER, sessionAlice))
                .when()
                        .get("/resources/" + resourceId.getResourceId() + "?game=" + game)
                .then().
                        statusCode(200)
                        .extract().response().asString();

        GameResource updatedResource = jacksonMapper.readValue(responseJSON, GameResource.class);
        Assert.assertEquals(resourceId.getResourceId(), gameResource.getId());
        Assert.assertEquals("alice", gameResource.getOwner());
        Assert.assertEquals(gameResource.getName(), updatedResource.getName());
        Assert.assertEquals(gameResource.getStatus(), updatedResource.getStatus());

        logger.info("Deleting the resource...");
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header(new Header(HttpSessionConfig.SESSION_HEADER, sessionAlice))
        .when()
                .delete("/resources/" + resourceId.getResourceId() + "?game=" + game)
                .then().
                statusCode(200);
        /* making sure it has been deleted */
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header(new Header(HttpSessionConfig.SESSION_HEADER, sessionAlice))
        .when()
                .get("/resources/" + resourceId.getResourceId() + "?game=" + game)
        .then()
                .statusCode(404);
    }

}
