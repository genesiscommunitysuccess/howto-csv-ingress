package csv_ingress.utilities.json_utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class JsonModifier {
    private static final Logger LOG = LoggerFactory.getLogger(CompareJSON.class);
    private static final String PARENT_OBJECT = "DETAILS";

    public static void modifyJSON(Path jsonFile, String key, String value) {
        LOG.info("Modifying JSON file: {}", jsonFile);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonFile.toFile());
            ObjectNode objectNode = (ObjectNode) jsonNode.get(PARENT_OBJECT);
            objectNode.put(key, value);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(), jsonNode);
        } catch (IOException e) {
            LOG.error("Error while modifying JSON file: {}", jsonFile, e);
            throw new RuntimeException(e);
        }
    }
}
