package com.master.meta.utils;

import com.master.meta.config.FileTransferConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class ScpUtils {
    private final FileTransferConfiguration fileTransferConfiguration;
    private final ThreadLocal<SshClient> clientThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<ClientSession> sessionThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<ScpClient> scpClientThreadLocal = new ThreadLocal<>();

    public ScpUtils(FileTransferConfiguration fileTransferConfiguration) {
        this.fileTransferConfiguration = fileTransferConfiguration;
    }

    /**
     * 初始化SSH客户端
     * <p>
     * 该方法创建一个新的SSH客户端实例，使用默认配置进行设置，
     * 启动客户端并将客户端对象存储到线程本地变量中以便后续使用。
     *
     */
    public void initClient() {
        SshClient existingClient = clientThreadLocal.get();
        if (existingClient != null) {
            // 如果客户端已经存在且正在运行，则先关闭它
            if (existingClient.isOpen()) {
                try {
                    existingClient.close();
                } catch (Exception e) {
                    // 忽略关闭异常，继续创建新客户端
                }
            }
            clientThreadLocal.remove();
        }

        SshClient client = null;
        try {
            client = SshClient.setUpDefaultClient();
            client.start();
            // 验证客户端是否成功启动
            if (client.isOpen()) {
                clientThreadLocal.set(client);
            } else {
                throw new RuntimeException("Failed to start SSH client");
            }
        } catch (Exception e) {
            // 如果创建或启动过程中出现异常，清理已创建的资源
            if (client != null) {
                try {
                    client.close();
                } catch (Exception closeException) {
                    // 忽略关闭异常
                }
            }
            throw new RuntimeException("Failed to initialize SSH client", e);
        }
    }

    /**
     * 建立SSH连接并创建SCP客户端
     *
     * @param host     主机地址
     * @param port     端口号
     * @param username 用户名
     * @param password 密码
     * @throws IOException 当连接失败或发生IO异常时抛出
     */
    public void connect(String host, int port, String username, String password) throws IOException {
        SshClient client = clientThreadLocal.get();
        ClientSession session = null;
        try {
            if (client == null) {
                throw new IllegalStateException("SSH client not initialized");
            }
            if (!client.isOpen()) {
                throw new IllegalStateException("SSH client is not started");
            }
            session = client.connect(username, host, port).verify().getSession();
            session.addPasswordIdentity(password);
            session.auth().verify();
            // 创建SCP客户端
            ScpClient scpClient = ScpClientCreator.instance().createScpClient(session);
            sessionThreadLocal.set(session);
            scpClientThreadLocal.set(scpClient);
        } catch (Exception e) {
            // 清理已创建的session资源
            if (session != null) {
                try {
                    session.close();
                } catch (Exception closeException) {
                    // 记录日志或忽略关闭异常
                }
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException("Failed to establish SSH connection", e);
            }
        }
    }

    /**
     * 上传本地文件到远程服务器指定路径
     *
     * @param localFilePath 本地文件路径，不能为空
     * @param remotePath    远程服务器目标路径，不能为空
     * @throws IOException 当参数无效、SSH会话不可用或文件操作失败时抛出
     */
    public void uploadFile(String localFilePath, String remotePath) throws IOException {
        if (localFilePath == null || localFilePath.trim().isEmpty()) {
            throw new IOException("Local file path cannot be null or empty");
        }
        if (remotePath == null || remotePath.trim().isEmpty()) {
            throw new IOException("Remote path cannot be null or empty");
        }
        ClientSession session = sessionThreadLocal.get();
        ScpClient scpClient = scpClientThreadLocal.get();
        if (session == null || !session.isOpen()) {
            throw new IOException("SSH session is not available or has been closed");
        }

        Path localPath = Paths.get(localFilePath);
        if (!Files.exists(localPath)) {
            throw new IOException("Local file does not exist: " + localFilePath);
        }
        if (!Files.isReadable(localPath)) {
            throw new IOException("Local file is not readable: " + localFilePath);
        }
        scpClient.upload(localPath, remotePath);
    }

    /**
     * 上传文件到远程服务器
     *
     * @param slaveConfig 远程服务器配置信息，包含本地文件路径、远程路径、主机地址、端口、用户名和密码等信息
     * @throws IOException 当本地文件路径或远程路径为空、SSH会话不可用、本地文件不存在或不可读时抛出异常
     */
    public void uploadFile(FileTransferConfiguration.SlaveConfig slaveConfig, String localPath, String remotePath) throws IOException {
        if (localPath == null || localPath.trim().isEmpty()) {
            throw new IOException("Local file path cannot be null or empty");
        }
        if (remotePath == null || remotePath.trim().isEmpty()) {
            throw new IOException("Remote path cannot be null or empty");
        }
        initClient();
        connect(slaveConfig.getHost(), slaveConfig.getPort(), slaveConfig.getUsername(), slaveConfig.getPassword());
        ClientSession session = sessionThreadLocal.get();
        ScpClient scpClient = scpClientThreadLocal.get();
        if (session == null || !session.isOpen()) {
            throw new IOException("SSH session is not available or has been closed");
        }

        Path localFIlePath = Paths.get(localPath);
        if (!Files.exists(localFIlePath)) {
            throw new IOException("Local file does not exist: " + localPath);
        }
        if (!Files.isReadable(localFIlePath)) {
            throw new IOException("Local file is not readable: " + localPath);
        }
        scpClient.upload(localFIlePath, remotePath);
    }

    // 下载文件
    public void downloadFile(String remoteFilePath, String localPath) throws IOException {
        ScpClient scpClient = scpClientThreadLocal.get();
        Path localDestination = Paths.get(localPath);
        scpClient.download(remoteFilePath, localDestination);
    }

    // 递归上传目录
    public void uploadDirectory(String localDirPath, String remotePath) throws IOException {
        ScpClient scpClient = scpClientThreadLocal.get();
        Path localPath = Paths.get(localDirPath);
        scpClient.upload(localPath, remotePath, ScpClient.Option.Recursive);
    }

    // 递归下载目录
    public void downloadDirectory(String remoteDirPath, String localPath) throws IOException {
        ScpClient scpClient = scpClientThreadLocal.get();
        Path localDestination = Paths.get(localPath);
        scpClient.download(remoteDirPath, localDestination, ScpClient.Option.Recursive);
    }

    // 关闭连接
    public void close() {
        ClientSession session = sessionThreadLocal.get();
        SshClient client = clientThreadLocal.get();
        try {
            if (session != null && !session.isClosed()) {
                session.close();
            }
            if (client != null && !client.isClosed()) {
                client.stop();
            }
        } catch (Exception e) {
            // 日志记录
            log.error("Error closing SSH connection: {}", e.getMessage(), e);
        } finally {
            sessionThreadLocal.remove();
            clientThreadLocal.remove();
            scpClientThreadLocal.remove();
        }
    }
}
