package com.master.meta;

import com.master.meta.utils.DateFormatUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateFormatUtilTest {
    // 测试正常的日期时间字符串转换
    @Test
    @DisplayName("测试正常格式的日期时间字符串转换")
    void testString2LocalDateTimeStyle2WithValidInput() {
        String validDateTimeStr = "2025-12-16 11:07:00";
        LocalDateTime expected = LocalDateTime.of(2025, 12, 16, 11, 7, 0);

        LocalDateTime result = DateFormatUtil.string2LocalDateTimeStyle2(validDateTimeStr);
        assertEquals(expected, result, "转换后的LocalDateTime应该与预期值相等");
    }
}
