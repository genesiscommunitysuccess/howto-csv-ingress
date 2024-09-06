package csv_ingress.utilities.sftpClient_utilities

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FileSystemFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths

/**
 * A utility class for handling SFTP operations.
 *
 * @property sftpHost The SFTP server host.
 * @property sftpUser The SFTP server username.
 * @property sftpPassword The SFTP server password.
 */
class SFTPClient(
    private val sftpHost: String,
    private val sftpUser: String,
    private val sftpPassword: String
) {
    private val LOG: Logger = LoggerFactory.getLogger(SFTPClient::class.java)

    /**
     * Establishes a connection to the SFTP server.
     *
     * @return An instance of [SSHClient] connected to the SFTP server.
     */
    fun establishConnection(): SSHClient {
        val ssh = SSHClient()
        ssh.connectTimeout = 2000
        ssh.timeout = 2000
        ssh.addHostKeyVerifier(PromiscuousVerifier())
        try {
            ssh.connect(sftpHost, 22)
            ssh.authPassword(sftpUser, sftpPassword)
            LOG.info("Connected to SFTP the host: $sftpHost with username: $sftpUser")
        } catch (e: Exception) {
            LOG.error("Error connecting to SFTP: $e")
            ssh.disconnect()
        }
        return ssh
    }

    /**
     * Downloads a file from the SFTP server to the specified destination.
     *
     * @param sshClient The [SSHClient] instance connected to the SFTP server.
     * @param source The source file path on the SFTP server.
     * @param dest The destination file path on the local machine.
     */
    private fun downloadFileToDest(sshClient: SSHClient, source: String, dest: String): Boolean {
        sshClient.startSession().use {
            val scp = sshClient.newSCPFileTransfer()
            Files.createDirectories(Paths.get(dest))
            assert(Files.exists(Paths.get(dest)))
            scp.download(source, FileSystemFile(dest))
            return true
        }
    }

    /**
     * Downloads a file from the SFTP server.
     *
     * @param source The source file path on the SFTP server.
     * @param dest The destination file path on the local machine.
     * @return `true` if the file was downloaded successfully, `false` otherwise.
     */
    fun downloadFileFromSFTP(sshClient: SSHClient, source: String, dest: String): Boolean {
        try {
            sshClient.startSession().use { session ->
                val cmd = session.exec("ls -lt")
                val output = cmd.inputStream.bufferedReader().readText()
                LOG.info("Output: \n$output")
            }
            return downloadFileToDest(sshClient, source, dest)
        } catch (e: Exception) {
            LOG.error("Error downloading file from SFTP: $e")
            return false
        } finally {
            sshClient.disconnect()
        }
    }

    /**
     * Uploads a file to the SFTP server.
     *
     * @param source The source file path on the local machine.
     * @param dest The destination file path on the SFTP server.
     * @return `true` if the file was uploaded successfully, `false` otherwise.
     */
    fun uploadFileToSFTP(sshClient: SSHClient, source: String, dest: String): Boolean {
        try {
            sshClient.newSFTPClient().use { sftpClient ->
                sftpClient.put(source, dest)
                LOG.info("Uploaded file from $source to SFTP server at $dest")
                return true
            }
        } catch (e: Exception) {
            LOG.error("Error uploading file to SFTP: $e")
            return false
        } finally {
            sshClient.disconnect()
        }
    }

    /**
     * Checks if a file with the specified name exists in the given SFTP server directory.
     *
     * @param sshClient The [SSHClient] instance connected to the SFTP server.
     * @param filePath The path to the directory on the SFTP server where the file should be checked.
     * @return `true` if a file with the specified name exists in the directory, `false` otherwise.
     */
    fun checkFileExistsInSFTP(sshClient: SSHClient, filePath: String): Boolean {
        try {
            sshClient.newSFTPClient().use { sftpClient ->
                val file = sftpClient.stat(filePath)
                return file != null
            }
        } catch (e: Exception) {
            LOG.error("Error checking if file exists on SFTP: $e")
            return false
        } finally {
            sshClient.disconnect()
        }
    }

}