package csv_ingress.utilities.file_utilities;

import com.github.dockerjava.api.DockerClient;
import csv_ingress.utilities.amazonS3Client_utilities.AmazonS3Client;
import csv_ingress.utilities.dockerClient_utilities.DockerContainerManager;
import csv_ingress.utilities.sftpClient_utilities.SFTPClient;
import net.schmizz.sshj.SSHClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Path;

import static csv_ingress.utilities.file_utilities.LocalFileUtilities.copyToDestination;
import static csv_ingress.utilities.file_utilities.LocalFileUtilities.fileExists;


/**
 * A utility class for handling file transfers to various destinations such as SFTP, S3, and local file systems.
 */
public class FileTransferClientFactory {
    private static SFTPClient sftpClient;
    private static SSHClient sshClient;
    private static AmazonS3Client amazonS3Client;
    private static S3Client s3Client;
    private static DockerContainerManager dockerUtilClient;
    private static DockerClient dockerClient;

    /**
     * Processes the connection parameters.
     *
     * @param connectionParams The connection parameters as a comma-separated string.
     * @return An array of connection parameters.
     */
    private static String[] processConnectionParams(String connectionParams) {
        return connectionParams == null ? null : connectionParams.split(",");
    }

    /**
     * Creates a client of the specified type using the provided connection parameters.
     *
     * @param type             The class type of the client (e.g., SSHClient.class, S3Client.class).
     * @param connectionParams The connection parameters as a comma-separated string.
     * @param <T>              The type of the client.
     * @return An instance of the specified client type.
     * @throws IllegalArgumentException If the client type is unsupported.
     */
    public static <T> T createClient(Class<T> type, String connectionParams) {
        String[] paramArr = processConnectionParams(connectionParams);
        switch (type.getSimpleName()) {
            case "SSHClient":
                sftpClient = new SFTPClient(paramArr[0], paramArr[1], paramArr[2]);
                sshClient = sftpClient.establishConnection();
                return type.cast(sshClient);
            case "S3Client":
                amazonS3Client = new AmazonS3Client(paramArr[0], paramArr[1]);
                s3Client = amazonS3Client.establishConnection();
                return type.cast(s3Client);
            case "DockerClient":
                dockerUtilClient = new DockerContainerManager(paramArr[0]);
                dockerClient = dockerUtilClient.establishConnection();
                return type.cast(dockerClient);
            default:
                throw new IllegalArgumentException("Unsupported client type: " + type);
        }
    }

    /**
     * Uploads a file to the specified client type.
     *
     * @param type        The class type of the client (e.g., SSHClient.class, S3Client.class).
     * @param filename    The name of the file to be uploaded.
     * @param destination The destination path where the file should be uploaded.
     */
    public static <T> Boolean uploadFileToClient(Class<T> type, String filename, String destination) {
        return switch (type.getSimpleName()) {
            case "SSHClient" -> sftpClient.uploadFileToSFTP(sshClient, filename, destination);
            case "S3Client" -> amazonS3Client.uploadFileToS3(s3Client, filename);
            case "LocalFileUtilities" -> copyToDestination(Path.of(filename), Path.of(destination));
            case "DockerClient" -> dockerUtilClient.uploadFileToContainer(dockerClient, filename, destination);
            default -> throw new IllegalArgumentException("Unsupported client type: " + type);
        };
    }

    /**
     * Checks if a file exists in the specified client type.
     * *
     * * @param type        The class type of the client (e.g., SSHClient.class, S3Client.class).
     * * @param fileName    The name of the file to check.
     * * @param destination The destination path where the file should be checked.
     * * @param timeout     The timeout in seconds.
     */
    public static <T> Boolean checkFileExists(Class<T> type, String fileName, String destination, String timeout) {
        long longTimeOut = Long.parseLong(timeout.isEmpty() ? "0" : timeout);
        return switch (type.getSimpleName()) {
            case "SSHClient" -> sftpClient.checkFileExistsInSFTP(sshClient, destination);
            case "S3Client" -> amazonS3Client.checkFileExistsInS3(s3Client, fileName);
            case "LocalFileUtilities" -> fileExists(Path.of(destination), fileName, longTimeOut);
            case "DockerClient" -> dockerUtilClient.verifyFolderExists(dockerClient, destination,fileName, longTimeOut);
            default -> throw new IllegalArgumentException("Unsupported client type: " + type);
        };
    }
}
