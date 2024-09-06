package csv_ingress.utilities.api_utitilites.event_base;

import csv_ingress.utilities.config_utilities.ConfigReader;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.UUID;

import static csv_ingress.utilities.api_utitilites.request_response_spec.RequestResponseSpecifications.requestSpecification;
import static csv_ingress.utilities.api_utitilites.request_response_spec.RequestResponseSpecifications.responseSpecification;
import static io.restassured.RestAssured.given;


public abstract class EventBase {
    protected static final ConfigReader config_Properties = new ConfigReader("src/test/resources/4-config/config.properties");
    private static final Logger LOG = LoggerFactory.getLogger(EventBase.class);
    private final Path bodyPath;
    private String endPoint;
    private String sourceRef;

    public EventBase(Path bodyPath) {
        this.bodyPath = bodyPath;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    protected String getSourceRef() {
        if (this.sourceRef == null) {
            this.sourceRef = UUID.randomUUID().toString();
        }
        return this.sourceRef;
    }

    public Response post() {
        try {
            return
                    given()
                            .spec(requestSpecification(this.bodyPath))
                            .header("SOURCE-REF", getSourceRef()).
                    when()
                            .post(getEndPoint()).
                    then()
                            .spec(responseSpecification())
                            .extract().response();
        } catch (Exception e) {
            LOG.error("Error in post - {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Response get() {
        try {
            return
                    given()
                            .spec(requestSpecification(this.bodyPath))
                            .header("SOURCE-REF", getSourceRef()).
                    when()
                            .get(this.endPoint).
                    then()
                            .spec(responseSpecification())
                            .extract().response();
        } catch (Exception e) {
            LOG.error("Error in get - {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Response put() {
        try {
            return
                    given()
                            .spec(requestSpecification(this.bodyPath))
                            .header("SUBSCRIPTION-REF", getSourceRef()).
                    when()
                            .put(this.endPoint).
                    then()
                            .spec(responseSpecification())
                            .extract().response();
        } catch (Exception e) {
            LOG.error("Error in put - {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public Response delete() {
        try {
            return
                    given()
                            .spec(requestSpecification(this.bodyPath))
                            .header("SOURCE-REF", getSourceRef()).
                    when()
                            .delete(this.endPoint).
                    then()
                            .spec(responseSpecification())
                            .extract().response();
        } catch (Exception e) {
            LOG.error("Error in delete - {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}