package csv_ingress.utilities.json_utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static csv_ingress.utilities.file_utilities.FileConstants.generateJSONFilePath;

/**
 * Utility class for reading and writing JSON files.
 */
public class WriteReadJSON {
    private static final Logger LOG = LoggerFactory.getLogger(WriteReadJSON.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Retrieves the JSON content from the response body, writes it to a file, and returns the file path.
     *
     * @param response the response object containing the JSON data
     * @param filename the filename to name of the file to create
     * @param expectedOrActual a string indicating whether the data is expected or actual
     * @return the file path where the JSON data was written
     */
    public static Path retrieveJSONFromResponseBody(Response response, String filename, String expectedOrActual) {
        Map<String, Object> expected = response.jsonPath().get("");
        Path expectedFilePath = generateJSONFilePath(expectedOrActual, filename);
        writeJSON(expectedFilePath, expected);
        return expectedFilePath;
    }

    /**
     * Writes a JSON representation of the provided map to a file at the specified file path.
     *
     * @param filePath the path where the JSON file will be written
     * @param map the map containing the data to be written as JSON
     * @throws RuntimeException if an I/O error occurs while writing the JSON to the file
     */
    public static void writeJSON(Path filePath, Map<String, Object> map) {
        try {
            Files.createDirectories(filePath.getParent());
            try (FileWriter fileWriter = new FileWriter(filePath.toFile())) {
                fileWriter.write(objectMapper.writeValueAsString(map));
                LOG.info("JSON written successfully to file: {}", filePath);
            }
        } catch (IOException e) {
            LOG.error("Error writing JSON to file: {}", filePath, e);
            throw new RuntimeException(e);
        }
    }
}