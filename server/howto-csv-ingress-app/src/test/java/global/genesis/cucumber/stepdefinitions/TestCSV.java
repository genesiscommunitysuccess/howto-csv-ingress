package global.genesis.cucumber.stepdefinitions;

import com.github.dockerjava.api.DockerClient;
import csv_ingress.utilities.api_utitilites.events.authentication.EventLoginAuth;
import csv_ingress.utilities.api_utitilites.reqrep.ReqRep;
import csv_ingress.utilities.config_utilities.ConfigReader;
import csv_ingress.utilities.file_utilities.LocalFileUtilities;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.schmizz.sshj.SSHClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Path;
import java.nio.file.Paths;

import static csv_ingress.utilities.api_utitilites.constants.Authentication.SESSION_AUTH_TOKEN;
import static csv_ingress.utilities.file_utilities.FileConstants.feature;
import static csv_ingress.utilities.json_utilities.CompareJSON.compareJSONFiles;
import static csv_ingress.utilities.file_utilities.FileTransferClientFactory.*;
import static csv_ingress.utilities.json_utilities.WriteReadJSON.retrieveJSONFromResponseBody;

public class TestCSV {
    private static final Logger LOG = LoggerFactory.getLogger(TestCSV.class);
    private static final ConfigReader config_properties = new ConfigReader("src/test/resources/4-config/config.properties");
    private boolean isLocal = false;
    private boolean isUploaded = false;
    private SSHClient sftpClient;
    private S3Client amazonS3Client;
    private DockerClient dockerClient;

    @Given("I connect to {string} with a connection {string}")
    public void i_connect_to_external_location_with_a_connection_type(String externalLocation, String connectionParams) {
        switch (externalLocation) {
            case "SFTP" -> sftpClient = createClient(SSHClient.class, connectionParams);
            case "AmazonS3" -> amazonS3Client = createClient(S3Client.class, connectionParams);
            case "Docker" -> dockerClient = createClient(DockerClient.class, connectionParams);
            case "Local" -> isLocal = true;
        }
    }

    @When("I copy a CSV file named {string} to {string}")
    public void i_copy_a_valid_csv_file_named_filename_to_file_destination(String filename, String destination) {
        if (isLocal) isUploaded = uploadFileToClient(LocalFileUtilities.class, filename, destination);
        else if (sftpClient != null) isUploaded = uploadFileToClient(SSHClient.class, filename, destination);
        else if (amazonS3Client != null) isUploaded = uploadFileToClient(S3Client.class, filename, destination);
        else if (dockerClient != null) isUploaded = uploadFileToClient(DockerClient.class, filename, destination);
    }

    @Then("I should get a success confirmation")
    public void i_should_get_a_success_confirmation() {
        if (!isUploaded) {
            LOG.error("File was not uploaded successfully");
            throw new RuntimeException("File was not uploaded successfully");
        }
    }

    @Then("I should see the file name {string} in the folder {string} within timeout {string} seconds")
    public void i_should_see_the_file_name_filename_in_the_folder_folder_done(String filePath, String destination, String timeout) {
        final String fileName = Paths.get(filePath).getFileName().toString();
        boolean fileExists = false;
        if (isLocal) fileExists = checkFileExists(LocalFileUtilities.class, fileName, destination, timeout);
        else if (sftpClient != null) fileExists = checkFileExists(SSHClient.class, fileName, destination, timeout);
        else if (amazonS3Client != null) fileExists = checkFileExists(S3Client.class, fileName, destination, timeout);
        else if (dockerClient != null) fileExists = checkFileExists(DockerClient.class, fileName, destination, timeout);

        if (fileExists) {
            LOG.info("File {} exists in the folder: {}", fileName, destination);
        } else {
            throw new AssertionError("File " + filePath + " does not exist in the folder " + destination);
        }
    }

    @Then("I should see an error message {string}")
    public void i_should_see_an_error_message(String errorMessage) {
        LOG.error(errorMessage); // TODO: Implement this whenever the error message is ready
    }

    @And("I verify that the data from the file {string} is loaded into the database")
    public void iVerifyThatTheDataFromTheFileIsLoadedIntoTheDatabase(String filePath) {
        EventLoginAuth eventLoginAuth = new EventLoginAuth(config_properties.readProperty("username"), config_properties.readProperty("password"));
        SESSION_AUTH_TOKEN = eventLoginAuth.post().jsonPath().getString("SESSION_AUTH_TOKEN");

        final String fileName = Paths.get(filePath).getFileName().toString().replace(".", "");
        final String jsonFileName = filePath.contains("simple") ? "simple" + fileName : "snapshot" + fileName;
        ReqRep reqRep = null;
        Response response = null;
        if (fileName.contains("Settlement")) {
            reqRep = new ReqRep("REQ_EXCHANGE_HOLIDAY", "HOLIDAY_TYPE=Settlement");
            response = reqRep.get();
        }
        if (fileName.contains("Trading")) {
            reqRep = new ReqRep("REQ_EXCHANGE_HOLIDAY", "HOLIDAY_TYPE=Trading");
            response = reqRep.get();
        }
        Path actualJson = retrieveJSONFromResponseBody(response, jsonFileName, "actual");
        Path expectedJson = Path.of(config_properties.readProperty("expectedPath") + "/" + feature + "/" + jsonFileName + ".json");
        compareJSONFiles(expectedJson, actualJson, "RECORD_ID,TIMESTAMP");
    }
}
