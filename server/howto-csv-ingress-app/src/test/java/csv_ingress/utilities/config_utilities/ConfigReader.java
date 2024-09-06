package csv_ingress.utilities.config_utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConfigReader {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigReader.class);
    private final Properties properties = new Properties();

    /**
     * Constructor that loads the properties file.
     *
     * @param filePath the path to the properties file
     */
    public ConfigReader(String filePath) {
        try (FileInputStream file = new FileInputStream(filePath)) {
            properties.load(file);
        } catch (IOException e) {
            LOG.error("Error in reading the properties file: {}", e.getMessage());
            throw new RuntimeException("Error in reading the properties file: " + e.getMessage());
        }
    }

    /**
     * Reads the value associated with the given key from the properties file.
     *
     * @param keyWord the key whose associated value is to be returned
     * @return the value associated with the specified key, or null if the key is not foundF
     */
    public String readProperty(String keyWord) {
        return properties.getProperty(keyWord);
    }

    /**
     * Reads the integer value associated with the given key from the properties file.
     *
     * @param keyWord the key whose associated integer value is to be returned
     * @return the integer value associated with the specified key
     * @throws NumberFormatException if the value associated with the key is not a valid integer
     */
    public Integer readPropertyInt(String keyWord) {
        String value = properties.getProperty(keyWord);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOG.error("Invalid integer format for key {}: {}", keyWord, value);
                throw e;
            }
        }
        return null;
    }

    /**
     * Searches for keys that contain the given search term.
     *
     * @param searchTerm the term to search for in the keys
     * @return a map of matching keys and their associated values
     */
    public Map<String, String> searchKeys(String searchTerm) {
        return properties.entrySet().stream()
                .filter(entry -> entry.getKey().toString().contains(searchTerm))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString()
                ));
    }

    public String getValue(String keyWord, Predicate<String> condition) {
        Map<String, String> configFileMap = searchKeys(keyWord.toUpperCase());
        for (Map.Entry<String, String> entry : configFileMap.entrySet()) {
            String configKey = entry.getKey();
            String configVal = entry.getValue();
            if (condition.test(configKey)) {
                return configVal;
            }
        }
        return null;
    }

    /**
     * Retrieves all keys from the properties file.
     *
     * @return a Set of all keys
     */
    public Set<String> getAllKeys() {
        return properties.stringPropertyNames();
    }
}

