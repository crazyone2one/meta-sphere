package com.master.meta.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Created by 11's papa on 2025/10/11
 */
public class JSON {
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
            .build();
    private static final TypeFactory typeFactory = objectMapper.getTypeFactory();

    public static final int DEFAULT_MAX_STRING_LEN = Integer.MAX_VALUE;

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        // 支持json字符中带注释符
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // 自动检测所有类的全部属性
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        // 如果一个对象中没有任何的属性，那么在序列化的时候就会报错
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        // 设置JSON处理字符长度限制
        objectMapper.getFactory()
                .setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(DEFAULT_MAX_STRING_LEN).build());
        // 处理时间格式
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param value 需要转换为JSON字符串的对象
     * @return 转换后的JSON字符串
     * @throws RuntimeException 当对象转换失败时抛出
     */
    public static String toJSONString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJSONStringWithoutNull(Object value) {
        try {
            ObjectMapper tempMapper = objectMapper.copy();
            tempMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return tempMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String content, TypeReference<T> valueType) {
        try {
            JavaType subType = typeFactory.constructType(valueType);
            CollectionType javaType = typeFactory.constructCollectionType(List.class, subType);
            return objectMapper.readValue(content, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象序列化为JSON格式的字节数组
     *
     * @param object 需要序列化为JSON的对象
     * @return JSON格式的字节数组
     * @throws RuntimeException 当序列化过程中发生IO异常时抛出
     */
    public static byte[] toJSONBytes(Object object) {
        try {
            // 使用objectMapper将对象写入为字节数组
            return objectMapper.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String content, Class<T> valueType) {
        CollectionType javaType = typeFactory.constructCollectionType(List.class, valueType);
        try {
            return objectMapper.readValue(content, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> parseMap(String jsonObject) {
        try {
            return objectMapper.readValue(jsonObject, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Function<String, Map<String, Object>> jsonToMap = json -> {
        try {
            return json == null ? null : objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    };
}
