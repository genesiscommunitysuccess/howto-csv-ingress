package csv_ingress.utilities.api_utitilites.request_response_spec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;
import csv_ingress.utilities.config_utilities.ConfigReader;

import java.nio.file.Path;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static csv_ingress.utilities.api_utitilites.constants.Authentication.SESSION_AUTH_TOKEN;

public class RequestResponseSpecifications {
    private static final ConfigReader configReader = new ConfigReader("src/test/resources/4-config/config.properties");

    public static RequestSpecification requestSpecification(Object body) {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return given()
                    .baseUri(configReader.readProperty("defaultAPIHost"))
                    .log().uri()
                    .log().headers()
                    .header("SESSION_AUTH_TOKEN", SESSION_AUTH_TOKEN)
                    .contentType(ContentType.JSON)
                    .body(objectWriter.writeValueAsString(body))
                    .relaxedHTTPSValidation();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static RequestSpecification requestSpecification(Path bodyPath) {
        return given()
                .baseUri(configReader.readProperty("defaultAPIHost"))
                .log().uri()
                .log().headers()
                .header("SESSION_AUTH_TOKEN", SESSION_AUTH_TOKEN)
                .contentType(ContentType.JSON)
                .body(bodyPath.toFile())
                .relaxedHTTPSValidation();
    }

    public static ResponseSpecification responseSpecification() {
        return expect()
                .log().ifError()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .time(Matchers.lessThan(5000L));
    }
}
