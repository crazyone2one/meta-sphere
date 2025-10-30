package com.master.meta.utils;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Created by 11's papa on 2025/10/29
 */
public class ExcelExportUtil {
    public static <T> void export(HttpServletResponse response, List<T> data, String fileName) throws IOException {
        if (Objects.isNull(data) || data.isEmpty()) {
            throw new IllegalArgumentException("导出数据不能为空");
        }
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + ".xlsx");
        if (data.getFirst() instanceof Map<?, ?>) {
            writeMapData(response, (List<Map<String, Object>>) data);
        } else {
            writeObjectData(response, data);
        }
    }

    private static <T> void writeObjectData(HttpServletResponse response, List<T> data) throws IOException {
        Class<?> clazz = data.getFirst().getClass();
        List<List<String>> head = new ArrayList<>();
        List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
        fields.forEach(field -> head.add(Collections.singletonList(field.getName())));
        List<List<Object>> rows = new ArrayList<>();
        data.forEach(row -> {
            List<Object> rowData = new ArrayList<>();
            fields.forEach(field -> {
                field.setAccessible(true);
                try {
                    rowData.add(Optional.ofNullable(field.get(row)).orElse(""));
                } catch (IllegalAccessException e) {
                    rowData.add("");
                }
            });
            rows.add(rowData);
        });
        EasyExcel.write(response.getOutputStream()).head(head).sheet("Sheet1").doWrite(rows);
    }

    private static void writeMapData(HttpServletResponse response, List<Map<String, Object>> data) throws IOException {
        List<List<String>> head = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();
        Set<String> headers = data.getFirst().keySet();
        headers.forEach(k -> head.add(Collections.singletonList(k)));
        data.forEach(row -> {
            List<Object> rowData = new ArrayList<>();
            headers.forEach(k -> rowData.add(row.getOrDefault(k, "")));
            rows.add(rowData);
        });
        EasyExcel.write(response.getOutputStream()).head(head).sheet("Sheet1").doWrite(rows);
    }
}
