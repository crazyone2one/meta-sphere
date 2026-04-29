package com.master.meta;

import com.master.meta.utils.ShiftUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ShiftCrossDayTest {

    @Test
    public void testCrossDayShiftCalculation() {
        // 创建标准三班倒班次
        var shifts = ShiftUtils.createStandardThreeShifts();

        // 查找中班（16:00:00 - 01:00:00）
        var middleShift = shifts.stream()
                .filter(shift -> "中班".equals(shift.shiftName()))
                .findFirst()
                .orElse(null);

        assertNotNull(middleShift);
        assertEquals("16:00:00", middleShift.startTime().toString());
        assertEquals("01:00:00", middleShift.endTime().toString());

        // 测试跨天情况：当前日期是2026年1月10日，在0点到1点之间应该使用2026年1月9日
        LocalDateTime currentTime = LocalDateTime.of(2026, 1, 10, 0, 30, 0); // 2026-01-10 00:30:00
        
        // 获取班次开始和结束时间
        var shiftStart = ShiftUtils.getShiftStartDateTime(middleShift, currentTime);
        var shiftEnd = ShiftUtils.getShiftEndDateTime(middleShift, currentTime);
        
        // 验证开始时间应该是前一天（2026-01-09 16:00:00）
        assertEquals(2026, shiftStart.getYear());
        assertEquals(1, shiftStart.getMonthValue());
        assertEquals(9, shiftStart.getDayOfMonth()); // 前一天
        assertEquals(16, shiftStart.getHour());
        assertEquals(0, shiftStart.getMinute());
        
        // 验证结束时间应该是当天（2026-01-10 01:00:00）
        assertEquals(2026, shiftEnd.getYear());
        assertEquals(1, shiftEnd.getMonthValue());
        assertEquals(10, shiftEnd.getDayOfMonth()); // 当天
        assertEquals(1, shiftEnd.getHour());
        assertEquals(0, shiftEnd.getMinute());

        // 再测试另一个场景：当前时间在16:00到23:59之间，应该使用同一天
        LocalDateTime currentTime2 = LocalDateTime.of(2026, 1, 10, 18, 30, 0); // 2026-01-10 18:30:00
        
        var shiftStart2 = ShiftUtils.getShiftStartDateTime(middleShift, currentTime2);
        var shiftEnd2 = ShiftUtils.getShiftEndDateTime(middleShift, currentTime2);
        
        // 验证开始时间应该是当天（2026-01-10 16:00:00）
        assertEquals(2026, shiftStart2.getYear());
        assertEquals(1, shiftStart2.getMonthValue());
        assertEquals(10, shiftStart2.getDayOfMonth()); // 当天
        assertEquals(16, shiftStart2.getHour());
        assertEquals(0, shiftStart2.getMinute());
        
        // 验证结束时间应该是第二天（2026-01-11 01:00:00）
        assertEquals(2026, shiftEnd2.getYear());
        assertEquals(1, shiftEnd2.getMonthValue());
        assertEquals(11, shiftEnd2.getDayOfMonth()); // 第二天
        assertEquals(1, shiftEnd2.getHour());
        assertEquals(0, shiftEnd2.getMinute());

        System.out.println("所有测试通过！");
        System.out.println("跨天时间测试 - 开始时间: " + shiftStart);
        System.out.println("跨天时间测试 - 结束时间: " + shiftEnd);
        System.out.println("非跨天时间测试 - 开始时间: " + shiftStart2);
        System.out.println("非跨天时间测试 - 结束时间: " + shiftEnd2);
    }
}