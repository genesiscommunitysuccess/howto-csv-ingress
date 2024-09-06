package csv_ingress.utilities.dockerClient_utilities;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CopyArchiveToContainerCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DockerContainerManager {

    private static final Logger LOG = LoggerFactory.getLogger(DockerContainerManager.class);
    private final String containerName;

    public DockerContainerManager(String containerName) {
        this.containerName = containerName;
    }

    public DockerClient establishConnection() {
        LOG.info("Establishing connection to Docker client...");
        try {
            DockerClient dockerClient = DockerClientBuilder.getInstance().build();
            LOG.info("Connection established.");
            return dockerClient;
        } catch (Exception e) {
            LOG.error("Failed to establish connection to Docker client. Error: ", e);
            throw new RuntimeException("Failed to establish connection to Docker client. Error: " + e);
        }
    }

    private String getContainerIdByName(DockerClient dockerClient) {
        LOG.info("Retrieving container ID for container name: {}", this.containerName);
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            if (container.getNames()[0].contains(this.containerName)) {
                return container.getId();
            }
        }
        LOG.error("Container with name {} not found.", this.containerName);
        return null;
    }

    public boolean uploadFileToContainer(DockerClient dockerClient, String filename, String destination) {
        LOG.info("Uploading file {} to container {} at path {}...", filename, this.containerName, destination);
        String containerId = getContainerIdByName(dockerClient);
        createDirectories(dockerClient, Paths.get(destination));
        try (CopyArchiveToContainerCmd copyCmd = dockerClient.copyArchiveToContainerCmd(containerId)) {
            copyCmd.withHostResource(filename).withRemotePath(destination).exec();
            LOG.info("File copied successfully to Docker container");
            return true;
        } catch (Exception e) {
            LOG.error("Failed to upload file to container. Error: ", e);
            throw new RuntimeException("Failed to upload file to container. Error: " + e + "\n" +
                    executeCommand(dockerClient, "ls -la " + destination));
        }
    }

    public boolean verifyFolderExists(DockerClient dockerClient, String folderDonePath, String filePath, long timeoutInSeconds) {
        LOG.info("Verifying if folder {} exists in container {} with a timeout of {} seconds...", folderDonePath, this.containerName, timeoutInSeconds);
        long startTime = System.currentTimeMillis();
        while (true) {
            List<String> outputLines = executeCommand(dockerClient, "ls -la " + folderDonePath);
            boolean fileExist = !outputLines.isEmpty();

            if (fileExist) {
                String fileName = Paths.get(filePath).getFileName().toString();
                if (outputLines.stream().anyMatch(line -> line.contains(fileName))) {
                    LOG.info("Folder {} exists in container {}.", folderDonePath, this.containerName);
                    deleteDirectories(dockerClient, Paths.get(folderDonePath).getParent());
                    return true;
                }
            }

            if (System.currentTimeMillis() - startTime > timeoutInSeconds * 1000) {
                LOG.error("Timeout reached. The hidden folder {} was not detected within {} seconds.", folderDonePath, timeoutInSeconds);
                throw new RuntimeException("Timeout reached. The hidden folder " + timeoutInSeconds + " was not detected within " + folderDonePath + " seconds.");
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void createDirectories(DockerClient dockerClient, Path path) {
        LOG.info("Creating directories {}", path);
        executeCommand(dockerClient, "mkdir -p " + path);
        LOG.info("Directories created successfully: {}", path);
    }

    private void deleteDirectories(DockerClient dockerClient, Path path) {
        LOG.info("Deleting file {}", path);
        executeCommand(dockerClient, "rm -rf " + path);
        LOG.info("File deleted successfully: {}", path);
    }

    public List<String> executeCommand(DockerClient dockerClient, String cmd) {
        LOG.info("Executing command {} in container {}...", cmd, this.containerName);
        String containerId = getContainerIdByName(dockerClient);
        List<String> outputLines = new ArrayList<>();
        String[] commandParts = cmd.split(" ");
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(commandParts)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();
        try {
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(new ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame item) {
                            outputLines.add(new String(item.getPayload()).trim());
                        }
                    }).awaitCompletion();
            LOG.info("Command executed successfully in container: {}, command results is: {}", this.containerName, outputLines);
            return outputLines;
        } catch (InterruptedException e) {
            LOG.error("Failed to execute command in container. Error: ", e);
            throw new RuntimeException("Failed to execute command in container. Error: " + e);
        }
    }
}
