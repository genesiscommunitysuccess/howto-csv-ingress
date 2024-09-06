package csv_ingress.utilities;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExecuteLinuxCmd {
    private static final Logger LOG = LoggerFactory.getLogger(ExecuteLinuxCmd.class);

    public static boolean executeCmd(String cmd) {
        List<String> output;
        int exitCode = 0;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", cmd);
            Process process = processBuilder.start();
            // Wait for the command to complete
            exitCode = process.waitFor();
            output = IOUtils.readLines(process.getInputStream(), StandardCharsets.UTF_8);
            LOG.info("Command executed successfully. Exit code: {}", exitCode);
            LOG.info("Output: {}", output);
            return true;
        } catch (IOException | InterruptedException e) {
            LOG.error("Error occurred while copying the file. Exit code: {}", exitCode);
            throw new RuntimeException(e);
        }
    }
}