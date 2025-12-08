package com.master.meta;

import com.master.meta.utils.ScpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class ScpTest {
    ScpUtils scpUtils = new ScpUtils();

    @Test
    void uploadTest() {
        try {
            scpUtils.initClient();
            // 连接远程服务器
            scpUtils.connect("172.16.2.15", 8841, "root", "zkah@123");
            // 上传文件
            scpUtils.uploadFile("C:\\Users\\the2n\\Desktop\\150622B0012000200092_CDDY_20241006163722.txt", "/home/app/luna");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scpUtils.close();
        }
    }
}
