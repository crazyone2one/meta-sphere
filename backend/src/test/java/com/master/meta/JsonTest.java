package com.master.meta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.master.meta.utils.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SpringBootTest
public class JsonTest {
    @Test
    void testJson() throws Exception {
        String json = "{\"走向长度\":\"\",\"倾向长度\":\"\",\"煤层厚度\":\"\",\"煤层倾角\":\"\",\"平均采高\":\"\",\"已采长度\":\"\",\"剩余长度\":\"\",\"自然发火期\":\"\",\"是否智能化综采面\":\"\",\"工作面风量\":\"\",\"上下顺槽支护方式\":\"\",\"隐蔽到致灾因素普查情况\":\"\",\"顶板垮落 （垮落带、弯曲下沉带、裂隙带）情况\":\"\"}";
        String fixed = fixEscapedJson(json);
        System.out.println(fixed); // 输出: {"名称":"","参数":"","用途":""}
        parseEscapedJson(json);
    }
    public static String fixEscapedJson(String escapedJson) {
        // 去除多余的反斜杠
        return escapedJson.replace("\\\"", "\"");
    }
    public static void parseEscapedJson(String escapedJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 先解析为字符串，再解析为实际对象
        String unescaped = mapper.readValue(escapedJson, String.class);
        Object result = mapper.readTree(unescaped);
        System.out.println(result);
    }
}
