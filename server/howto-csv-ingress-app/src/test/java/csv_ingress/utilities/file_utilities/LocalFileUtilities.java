package csv_ingress.utilities.file_utilities;

import csv_ingress.utilities.ExecuteLinuxCmd;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.stream;

/**
 * This class provides utility methods for copying files to a local destination and checking if a file exists in a local directory.
 */
public class LocalFileUtilities {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileUtilities.class);

    /**
     * Copies a file to the specified destination directory.
     *
     * @param sourceFilePath The path to the source file to be copied.
     * @param destinationDir The path to the destination directory where the file should be copied.
     * @return `true` if the file was successfully copied, `false` otherwise.
     */
    public static boolean copyToDestination(Path sourceFilePath, Path destinationDir) {
        try {
            deleteDirectory(destinationDir);
            makeDirectory(destinationDir);
            Path destination = destinationDir.resolve(sourceFilePath.getFileName());
            Files.copy(sourceFilePath, destination, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("File moved successfully to: {}", destination);
            return true;
        } catch (Exception e) {
            LOG.error("Error moving file to destination: {}", destinationDir, e);
            return false;
        }
    }

    /**
     * Checks if a file with the specified name exists in the given local directory.
     * Waits for the `.done` folder to be created within a timeout period before checking.
     *
     * @param destinationDir The path to the local directory to check in.
     * @param fileName       The name of the file to check for.
     * @param timeoutSec     The timeout period to wait for the `.done` folder, in seconds.
     * @return `true` if the file exists in the directory, `false` otherwise.
     */
    public static boolean fileExists(Path destinationDir, String fileName, long timeoutSec) {
        waitForDoneFolder(destinationDir, timeoutSec);
        if (Files.isDirectory(destinationDir)) {
            boolean isContainFile = stream(Objects.requireNonNull(destinationDir.toFile().listFiles()))
                    .filter(File::isFile)
                    .anyMatch(file -> file.getName().contains(fileName));
            deleteDirectory(destinationDir);
            return isContainFile;
        }
        return false;
    }

    /**
     * Waits for the `.done` folder to be created within a specified timeout period.
     *
     * @param doneFolder The path to the `.done` folder to wait for.
     * @param timeoutInSeconds The timeout period to wait for the `.done` folder, in seconds.
     * @throws RuntimeException if the thread is interrupted while waiting.
     */
    public static void waitForDoneFolder(Path doneFolder, long timeoutInSeconds) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (Files.exists(doneFolder)) {
                try {
                    if (Files.isHidden(doneFolder)) {
                        LOG.info("Hidden folder {} has been detected.", doneFolder);
                        break;
                    } else {
                        LOG.warn("Folder {} exists but is not hidden.", doneFolder);
                    }
                } catch (IOException e) {
                    LOG.error("Error checking if the folder is hidden.", e);
                }
            }

            if (System.currentTimeMillis() - startTime > timeoutInSeconds * 1000) {
                LOG.error("Timeout reached. The hidden folder {} was not detected within {} seconds.", doneFolder, timeoutInSeconds);
                break;
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deletes the specified directory if it exists.
     *
     * @param directory The path to the directory to be deleted.
     */
    protected static void deleteDirectory(Path directory) {
        try {
            if (Files.isDirectory(directory)) {
                FileUtils.deleteDirectory(directory.toFile());
            }
        } catch (IOException e) {
            LOG.error("Error deleting directory: {}", directory, e);
        }
    }

    /**
     * Creates the specified directory if it does not exist.
     *
     * @param directory The path to the directory to be created.
     */
    private static void makeDirectory(Path directory) {
        try {
            if (Files.notExists(directory)) {
                Files.createDirectories(directory);
            }
        } catch (IOException e) {
            LOG.error("Error creating directory: {}", directory, e);
        }
    }
}