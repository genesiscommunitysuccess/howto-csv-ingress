package csv_ingress.utilities.file_utilities;

import java.nio.file.Path;

public class FileConstants {
    public static final String feature = "CsvIngress";

    public static Path generateJSONFilePath(String expectedOrActual, String filename) {
        return Path.of("src/test/resources/"
                + (expectedOrActual.equalsIgnoreCase("expected") ? "3-" + expectedOrActual : "2-" + expectedOrActual)
                + "/" + feature
                + "/" + filename + ".json"
        );
    }
}