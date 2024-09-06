package csv_ingress.utilities.amazonS3Client_utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.file.Paths;

/**
 * This class provides utility methods for interacting with Amazon S3.
 * It includes methods to establish a connection, upload files, download files, and check if a file exists in an S3 bucket.
 */
public class AmazonS3Client {
    private static final Logger LOG = LoggerFactory.getLogger(AmazonS3Client.class);
    private final String regionName;
    private final String bucketName;

    public AmazonS3Client(String regionName, String bucketName) {
        this.regionName = regionName;
        this.bucketName = bucketName;
    }

    /**
     * Establishes a connection to the S3 service using the specified region and credentials provider.
     *
     * @return An instance of `S3Client` configured with the specified region and credentials provider.
     */
    public S3Client establishConnection() {
        return S3Client.builder()
                .region(Region.of(regionName))
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    /**
     * Uploads a file to the specified S3 bucket.
     *
     * @param s3Client       The S3Client instance used to interact with the S3 service.
     * @param sourceFilePath The path to the file to be uploaded to the S3 bucket.
     * @return `true` if the file was successfully uploaded, `false` otherwise.
     */
    public Boolean uploadFileToS3(S3Client s3Client, String sourceFilePath) {
        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(sourceFilePath)
                    .build();
            s3Client.putObject(objectRequest, RequestBody.fromFile(Paths.get(sourceFilePath)));
            LOG.info("File uploaded to S3");
        } catch (S3Exception e) {
            LOG.error("Error uploading file to S3: {}", e.getMessage());
            return false;
        } finally {
            s3Client.close();
        }
        return true;
    }

    /**
     * Downloads a file from the specified S3 bucket to the local destination.
     *
     * @param s3Client            The S3Client instance used to interact with the S3 service.
     * @param sourceFilePath      The path to the file in the S3 bucket to download.
     * @param destinationFilePath The local path where the downloaded file should be saved.
     * @return `true` if the file was successfully downloaded, `false` otherwise.
     */
    public Boolean downloadFileFromS3(S3Client s3Client, String sourceFilePath, String destinationFilePath) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(sourceFilePath)
                    .build();
            s3Client.getObject(getObjectRequest, Paths.get(destinationFilePath));
            LOG.info("File downloaded from S3");
        } catch (S3Exception e) {
            LOG.error("Error downloading file from S3: {}", e.getMessage());
            return false;
        } finally {
            s3Client.close();
        }
        return true;
    }

    /**
     * Checks if a file with the specified name exists in the given S3 bucket.
     *
     * @param s3Client       The S3Client instance used to interact with the S3 service.
     * @param sourceFilePath The path to the file in the S3 bucket to check for.
     * @return `true` if the file exists in the S3 bucket, `false` otherwise.
     */
    public Boolean checkFileExistsInS3(S3Client s3Client, String sourceFilePath) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(sourceFilePath)
                    .build();
            s3Client.getObject(getObjectRequest, Paths.get(sourceFilePath));
            LOG.info("File exists in S3");
        } catch (S3Exception e) {
            LOG.error("Error checking file in S3: {}", e.getMessage());
            return false;
        } finally {
            s3Client.close();
        }
        return true;

    }

}
