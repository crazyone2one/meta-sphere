package com.master.meta.utils;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScpUtils {
    private SshClient client;
    private ClientSession session;
    private ScpClient scpClient;

    // 初始化SSH客户端
    public void initClient() {
        client = SshClient.setUpDefaultClient();
        client.start();
    }
    // 建立连接
    public void connect(String host, int port, String username, String password) throws IOException {
        session = client.connect(username, host, port)
                .verify()
                .getSession();
        session.addPasswordIdentity(password);
        session.auth().verify();

        // 创建SCP客户端
        scpClient = ScpClientCreator.instance().createScpClient(session);
    }
    // 上传文件
    public void uploadFile(String localFilePath, String remotePath) throws IOException {
        Path localPath = Paths.get(localFilePath);
        scpClient.upload(localPath, remotePath);
    }
    // 下载文件
    public void downloadFile(String remoteFilePath, String localPath) throws IOException {
        Path localDestination = Paths.get(localPath);
        scpClient.download(remoteFilePath, localDestination);
    }

    // 递归上传目录
    public void uploadDirectory(String localDirPath, String remotePath) throws IOException {
        Path localPath = Paths.get(localDirPath);
        scpClient.upload(localPath, remotePath, ScpClient.Option.Recursive);
    }

    // 递归下载目录
    public void downloadDirectory(String remoteDirPath, String localPath) throws IOException {
        Path localDestination = Paths.get(localPath);
        scpClient.download(remoteDirPath, localDestination, ScpClient.Option.Recursive);
    }

    // 关闭连接
    public void close() {
        try {
            if (session != null) {
                session.close();
            }
            if (client != null) {
                client.stop();
            }
        } catch (Exception e) {
            // 日志记录
        }
    }
}
