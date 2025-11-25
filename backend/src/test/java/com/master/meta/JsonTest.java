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
        Map<String, Object> stringObjectMap = JSON.jsonToMap.apply(json);
//        System.out.println(stringObjectMap);
        Set<String> fieldsToExtract = new HashSet<>();
        for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
            fieldsToExtract.add(entry.getKey());
        }
//        System.out.println(fieldsToExtract);
        String result = extractFields(json, fieldsToExtract);
        System.out.println(result);
    }
    public static String extractFields(String json, Set<String> requiredFields) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode originalNode = mapper.readTree(json);

        ObjectNode resultNode = mapper.createObjectNode();
        for (String field : requiredFields) {
            resultNode.set(field, originalNode.get(field));
        }

        return mapper.writeValueAsString(resultNode);
    }
}
