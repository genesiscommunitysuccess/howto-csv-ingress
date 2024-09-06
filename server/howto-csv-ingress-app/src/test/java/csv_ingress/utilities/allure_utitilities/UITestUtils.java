package csv_ingress.utilities.allure_utitilities;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;

import static csv_ingress.utilities.file_utilities.FileConstants.feature;
import static global.genesis.cucumber.stepdefinitions.Hooks.config_properties;

/**
 * Utility class for capturing the last state of the GUI and attaching it to the report.
 */
public class UITestUtils {
    private static final Logger LOG = LoggerFactory.getLogger(UITestUtils.class);

    /**
     * This method is used to add the latest file in the actual directory as an attachment
     */
    public static void addAttachment() {
        Path path = Paths.get(config_properties.readProperty("actualPath") + "/" + feature + "/");
        if (Files.exists(path)) {
            try {
                Optional<Path> latestFilePath = Files.list(path)
                        .filter(Files::isRegularFile)
                        .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

                if (latestFilePath.isPresent()) {
                    Path filePath = latestFilePath.get();
                    Allure.attachment(filePath.getFileName().toString(), new String(Files.readAllBytes(filePath)));
                }
            } catch (IOException e) {
                LOG.error("Error adding attachment to allure report", e);
            }
        }
    }
}
