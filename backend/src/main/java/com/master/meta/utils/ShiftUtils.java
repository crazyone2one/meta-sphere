package com.master.meta.utils;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : 11's papa
 * @since : 2026/1/9, 星期五
 **/
public class ShiftUtils {

    /**
     * 班次时间段类
     */
    @Getter
    public static class ShiftPeriod {
        // Getters
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final String shiftName;

        public ShiftPeriod(String startTime, String endTime, String shiftName) {
            this.startTime = LocalTime.parse(startTime);
            this.endTime = LocalTime.parse(endTime);
            this.shiftName = shiftName;
        }

        public ShiftPeriod(LocalTime startTime, LocalTime endTime, String shiftName) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.shiftName = shiftName;
        }

        @Override
        public String toString() {
            return shiftName + ": " + startTime + " - " + endTime;
        }
    }

    /**
     * 根据班次和当前时间获取正确的班次开始日期时间
     * 对于跨天班次，如果当前时间在跨天时间段内，则开始时间使用前一天的日期
     *
     * @param shiftPeriod 班次时间段
     * @param currentTime 当前时间
     * @return 班次开始时间对应的正确日期时间
     */
    public static LocalDateTime getShiftStartDateTime(ShiftPeriod shiftPeriod, LocalDateTime currentTime) {
        if (shiftPeriod == null || currentTime == null) {
            return null;
        }

        // 检查班次是否跨天（例如：22:00 - 06:00）
        if (shiftPeriod.getStartTime().isAfter(shiftPeriod.getEndTime())) {
            // 跨天的情况：开始时间晚于结束时间
            LocalTime currentTimeOfDay = currentTime.toLocalTime();
            
            // 检查当前时间是否在班次内
            boolean isCurrentTimeInShift = isCurrentTimeInShift(currentTime, shiftPeriod);
            
            if (isCurrentTimeInShift) {
                // 如果当前时间在跨天班次内，且在结束时间段（即第二天的时间段），则开始时间应使用前一天
                if (currentTimeOfDay.isBefore(shiftPeriod.getEndTime())) {
                    // 当前时间在跨天后的结束时间段，开始时间应为前一天
                    return LocalDateTime.of(currentTime.toLocalDate().minusDays(1), shiftPeriod.getStartTime());
                } else {
                    // 当前时间在正常的开始时间段
                    return LocalDateTime.of(currentTime.toLocalDate(), shiftPeriod.getStartTime());
                }
            }
        }
        
        // 不跨天的情况或当前时间不在班次内
        return LocalDateTime.of(currentTime.toLocalDate(), shiftPeriod.getStartTime());
    }

    /**
     * 根据班次和当前时间获取正确的班次结束日期时间
     * 对于跨天班次，结束时间可能需要使用第二天的日期
     *
     * @param shiftPeriod 班次时间段
     * @param currentTime 当前时间
     * @return 班次结束时间对应的正确日期时间
     */
    public static LocalDateTime getShiftEndDateTime(ShiftPeriod shiftPeriod, LocalDateTime currentTime) {
        if (shiftPeriod == null || currentTime == null) {
            return null;
        }

        // 检查班次是否跨天（例如：22:00 - 06:00）
        if (shiftPeriod.getStartTime().isAfter(shiftPeriod.getEndTime())) {
            // 跨天的情况：开始时间晚于结束时间
            LocalTime currentTimeOfDay = currentTime.toLocalTime();
            
            // 检查当前时间是否在班次内
            boolean isCurrentTimeInShift = isCurrentTimeInShift(currentTime, shiftPeriod);
            
            if (isCurrentTimeInShift) {
                // 如果当前时间在跨天班次内，且在结束时间段（即第二天的时间段），则结束时间应为当前天
                if (currentTimeOfDay.isBefore(shiftPeriod.getEndTime())) {
                    // 当前时间在跨天后的结束时间段，结束时间应为当前天
                    return LocalDateTime.of(currentTime.toLocalDate(), shiftPeriod.getEndTime());
                } else {
                    // 当前时间在正常的开始时间段，结束时间应为第二天
                    return LocalDateTime.of(currentTime.toLocalDate().plusDays(1), shiftPeriod.getEndTime());
                }
            }
        }
        
        // 不跨天的情况或当前时间不在班次内
        return LocalDateTime.of(currentTime.toLocalDate(), shiftPeriod.getEndTime());
    }

    /**
     * 判断当前时间是否在指定的班次时间段内
     *
     * @param currentTime 当前时间
     * @param shiftPeriod 班次时间段
     * @return 如果当前时间在班次内返回true，否则返回false
     */
    public static boolean isCurrentTimeInShift(LocalDateTime currentTime, ShiftPeriod shiftPeriod) {
        if (currentTime == null || shiftPeriod == null) {
            return false;
        }

        LocalTime currentTimeOfDay = currentTime.toLocalTime();

        // 检查班次是否跨天（例如：22:00 - 06:00）
        if (shiftPeriod.getStartTime().isAfter(shiftPeriod.getEndTime())) {
            // 跨天的情况：开始时间晚于结束时间
            return currentTimeOfDay.isAfter(shiftPeriod.getStartTime()) ||
                    currentTimeOfDay.isBefore(shiftPeriod.getEndTime()) ||
                    currentTimeOfDay.equals(shiftPeriod.getStartTime());
        } else {
            // 不跨天的情况：开始时间早于结束时间
            return (currentTimeOfDay.isAfter(shiftPeriod.getStartTime()) &&
                    currentTimeOfDay.isBefore(shiftPeriod.getEndTime())) ||
                    currentTimeOfDay.equals(shiftPeriod.getStartTime()) ||
                    currentTimeOfDay.equals(shiftPeriod.getEndTime());
        }
    }

    /**
     * 判断当前时间是否在多个班次中的任意一个内
     *
     * @param currentTime  当前时间
     * @param shiftPeriods 班次时间段列表
     * @return 如果当前时间在任意一个班次内返回true，否则返回false
     */
    public static boolean isCurrentTimeInAnyShift(LocalDateTime currentTime, List<ShiftPeriod> shiftPeriods) {
        if (currentTime == null || shiftPeriods == null || shiftPeriods.isEmpty()) {
            return false;
        }

        for (ShiftPeriod shiftPeriod : shiftPeriods) {
            if (isCurrentTimeInShift(currentTime, shiftPeriod)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取当前时间所在的班次
     *
     * @param currentTime  当前时间
     * @param shiftPeriods 班次时间段列表
     * @return 返回当前时间所在的班次，如果不在任何班次内则返回null
     */
    public static ShiftPeriod getCurrentShift(LocalDateTime currentTime, List<ShiftPeriod> shiftPeriods) {
        if (currentTime == null || shiftPeriods == null || shiftPeriods.isEmpty()) {
            return null;
        }

        for (ShiftPeriod shiftPeriod : shiftPeriods) {
            if (isCurrentTimeInShift(currentTime, shiftPeriod)) {
                return shiftPeriod;
            }
        }

        return null;
    }

    /**
     * 创建标准三班倒班次安排
     * 早班：08:00 - 16:00
     * 中班：16:00 - 00:00
     * 夜班：00:00 - 08:00
     *
     * @return 标准三班倒班次列表
     */
    public static List<ShiftPeriod> createStandardThreeShifts() {
        List<ShiftPeriod> shifts = new ArrayList<>();
        shifts.add(new ShiftPeriod("08:00:00", "16:00:00", "早班"));
        shifts.add(new ShiftPeriod("16:00:00", "01:00:00", "中班"));
        shifts.add(new ShiftPeriod("01:00:00", "08:00:00", "夜班"));
        return shifts;
    }

    /**
     * 创建两班倒班次安排
     * 白班：08:00 - 20:00
     * 夜班：20:00 - 08:00
     *
     * @return 两班倒班次列表
     */
    public static List<ShiftPeriod> createStandardTwoShifts() {
        List<ShiftPeriod> shifts = new ArrayList<>();
        shifts.add(new ShiftPeriod("08:00:00", "20:00:00", "白班"));
        shifts.add(new ShiftPeriod("20:00:00", "08:00:00", "夜班"));
        return shifts;
    }

    /**
     * 创建自定义班次
     *
     * @param startTime 开始时间，格式如 "HH:mm:ss" 或 "HH:mm"
     * @param endTime   结束时间，格式如 "HH:mm:ss" 或 "HH:mm"
     * @param shiftName 班次名称
     * @return 自定义班次对象
     */
    public static ShiftPeriod createCustomShift(String startTime, String endTime, String shiftName) {
        // 如果时间格式是 HH:mm，则自动补全为 HH:mm:ss
        String formattedStartTime = formatTimeToSeconds(startTime);
        String formattedEndTime = formatTimeToSeconds(endTime);

        return new ShiftPeriod(formattedStartTime, formattedEndTime, shiftName);
    }

    /**
     * 将时间格式统一为 HH:mm:ss 格式
     */
    private static String formatTimeToSeconds(String timeStr) {
        if (timeStr == null) {
            throw new IllegalArgumentException("时间字符串不能为null");
        }

        if (timeStr.split(":").length == 2) {
            // 如果是 HH:mm 格式，添加秒数
            return timeStr + ":00";
        } else if (timeStr.split(":").length == 3) {
            // 如果已经是 HH:mm:ss 格式，直接返回
            return timeStr;
        } else {
            throw new IllegalArgumentException("时间格式不正确，请使用 HH:mm 或 HH:mm:ss 格式");
        }
    }

    // 示例用法
    public static void main(String[] args) {
        // 测试当前时间是否在班次内
        LocalDateTime now = LocalDateTime.now();

        // 创建标准三班倒
        List<ShiftPeriod> shifts = createStandardThreeShifts();

        System.out.println("当前时间: " + now);
        System.out.println("是否在班次内: " + isCurrentTimeInAnyShift(now, shifts));

        ShiftPeriod currentShift = getCurrentShift(now, shifts);
        if (currentShift != null) {
            System.out.println("当前所在班次: " + currentShift.getShiftName() +
                    " (" + currentShift.getStartTime() + " - " + currentShift.getEndTime() + ")");
        } else {
            System.out.println("当前不在任何班次内");
        }

        // 测试自定义班次
        ShiftPeriod customShift = createCustomShift("09:00", "17:30", "工作日班");
        System.out.println("自定义班次: " + customShift);
        System.out.println("当前时间是否在自定义班次内: " + isCurrentTimeInShift(now, customShift));
        
        // 测试跨天班次计算
        System.out.println("\n=== 跨天班次测试 ===");
        
        // 找到中班
        ShiftPeriod middleShift = shifts.stream()
                .filter(shift -> "中班".equals(shift.getShiftName()))
                .findFirst()
                .orElse(null);
                
        if (middleShift != null) {
            System.out.println("中班: " + middleShift);
            
            // 测试跨天情况：2026年1月10日的00:30（在0点到1点之间，应该使用2026年1月9日作为开始日期）
            LocalDateTime crossDayTime = LocalDateTime.of(2026, 1, 10, 0, 30, 0);
            System.out.println("测试时间（跨天）: " + crossDayTime);
            
            LocalDateTime shiftStart = getShiftStartDateTime(middleShift, crossDayTime);
            LocalDateTime shiftEnd = getShiftEndDateTime(middleShift, crossDayTime);
            
            System.out.println("班次开始时间（跨天）: " + shiftStart);
            System.out.println("班次结束时间（跨天）: " + shiftEnd);
            
            // 验证开始时间是否是前一天
            if (shiftStart != null && shiftStart.toLocalDate().equals(crossDayTime.toLocalDate().minusDays(1))) {
                System.out.println("✓ 跨天测试通过：开始时间使用了前一天");
            } else {
                System.out.println("✗ 跨天测试失败：开始时间未正确使用前一天");
            }
            
            // 测试非跨天情况：2026年1月10日的18:30（在16:00到23:59之间，应该使用同一天）
            LocalDateTime normalTime = LocalDateTime.of(2026, 1, 10, 18, 30, 0);
            System.out.println("\n测试时间（非跨天）: " + normalTime);
            
            LocalDateTime shiftStartNormal = getShiftStartDateTime(middleShift, normalTime);
            LocalDateTime shiftEndNormal = getShiftEndDateTime(middleShift, normalTime);
            
            System.out.println("班次开始时间（非跨天）: " + shiftStartNormal);
            System.out.println("班次结束时间（非跨天）: " + shiftEndNormal);
            
            // 验证开始时间是否是同一天
            if (shiftStartNormal != null && shiftStartNormal.toLocalDate().equals(normalTime.toLocalDate())) {
                System.out.println("✓ 非跨天测试通过：开始时间使用了同一天");
            } else {
                System.out.println("✗ 非跨天测试失败：开始时间未正确使用同一天");
            }
        }
    }
}