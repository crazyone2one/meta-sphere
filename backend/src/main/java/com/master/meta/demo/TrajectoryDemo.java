package com.master.meta.demo;

import com.master.meta.utils.TrajectoryGeneratorUtil;
import com.master.meta.utils.TrajectoryGeneratorUtil.TrajectoryPoint;

import java.util.List;

/**
 * 轨迹生成演示类
 * 展示如何使用TrajectoryGeneratorUtil生成各种类型的轨迹
 */
public class TrajectoryDemo {
    
    public static void main(String[] args) {
        System.out.println("=== 2000国家大地坐标系轨迹生成演示 ===\n");
        
        // 演示1: 随机游走轨迹
        demonstrateRandomWalkTrajectory();
        
        // 演示2: 圆形轨迹
        demonstrateCircularTrajectory();
        
        // 演示3: 线性轨迹
        demonstrateLinearTrajectory();
        
        // 演示4: 椭圆轨迹
        demonstrateEllipticalTrajectory();
        
        // 演示5: 综合轨迹示例
        demonstrateSampleTrajectory();
    }
    
    /**
     * 演示随机游走轨迹
     */
    private static void demonstrateRandomWalkTrajectory() {
        System.out.println("1. 随机游走轨迹演示:");
        System.out.println("   起始点: 北京附近 (39.9042°N, 116.4074°E, 50m)");
        System.out.println("   参数: 50个点, 基础步长100米, 随机性系数0.3");
        
        TrajectoryPoint startPoint = new TrajectoryPoint(39.9042, 116.4074, 50.0);
        List<TrajectoryPoint> trajectory = TrajectoryGeneratorUtil.generateRandomWalkTrajectory(
            startPoint, 50, 100.0, 0.3);
        
        System.out.println("   生成轨迹点 (前10个):");
        for (int i = 0; i < Math.min(10, trajectory.size()); i++) {
            TrajectoryPoint point = trajectory.get(i);
            System.out.printf("     点%d: (%.6f, %.6f, %.2fm)\n", i, 
                             point.getLatitude(), point.getLongitude(), point.getAltitude());
        }
        
        System.out.println("   ... 总共生成了 " + trajectory.size() + " 个轨迹点\n");
    }
    
    /**
     * 演示圆形轨迹
     */
    private static void demonstrateCircularTrajectory() {
        System.out.println("2. 圆形轨迹演示:");
        System.out.println("   中心点: 北京附近 (39.9042°N, 116.4074°E, 50m)");
        System.out.println("   参数: 半径200米, 60个点, 旋转2圈");
        
        TrajectoryPoint centerPoint = new TrajectoryPoint(39.9042, 116.4074, 50.0);
        List<TrajectoryPoint> circularTrajectory = TrajectoryGeneratorUtil.generateCircularTrajectory(
            centerPoint, 200.0, 60, 2);
        
        System.out.println("   生成轨迹点 (前10个):");
        for (int i = 0; i < Math.min(10, circularTrajectory.size()); i++) {
            TrajectoryPoint point = circularTrajectory.get(i);
            System.out.printf("     点%d: (%.6f, %.6f, %.2fm)\n", i, 
                             point.getLatitude(), point.getLongitude(), point.getAltitude());
        }
        
        System.out.println("   ... 总共生成了 " + circularTrajectory.size() + " 个轨迹点\n");
    }
    
    /**
     * 演示线性轨迹
     */
    private static void demonstrateLinearTrajectory() {
        System.out.println("3. 线性轨迹演示:");
        System.out.println("   起始点: (39.9042°N, 116.4074°E, 50m)");
        System.out.println("   结束点: (39.9142°N, 116.4174°E, 70m)");
        System.out.println("   参数: 30个点");
        
        TrajectoryPoint startPoint = new TrajectoryPoint(39.9042, 116.4074, 50.0);
        TrajectoryPoint endPoint = new TrajectoryPoint(39.9142, 116.4174, 70.0);
        List<TrajectoryPoint> linearTrajectory = TrajectoryGeneratorUtil.generateLinearTrajectory(
            startPoint, endPoint, 30);
        
        System.out.println("   生成轨迹点 (前10个和后5个):");
        for (int i = 0; i < Math.min(10, linearTrajectory.size()); i++) {
            TrajectoryPoint point = linearTrajectory.get(i);
            System.out.printf("     点%d: (%.6f, %.6f, %.2fm)\n", i, 
                             point.getLatitude(), point.getLongitude(), point.getAltitude());
        }
        
        if (linearTrajectory.size() > 10) {
            System.out.println("     ...");
            for (int i = Math.max(10, linearTrajectory.size() - 5); i < linearTrajectory.size(); i++) {
                TrajectoryPoint point = linearTrajectory.get(i);
                System.out.printf("     点%d: (%.6f, %.6f, %.2fm)\n", i, 
                                 point.getLatitude(), point.getLongitude(), point.getAltitude());
            }
        }
        
        System.out.println("   总共生成了 " + linearTrajectory.size() + " 个轨迹点\n");
    }
    
    /**
     * 演示椭圆轨迹
     */
    private static void demonstrateEllipticalTrajectory() {
        System.out.println("4. 椭圆轨迹演示:");
        System.out.println("   中心点: 北京附近 (39.9042°N, 116.4074°E, 50m)");
        System.out.println("   参数: 长半轴300米, 短半轴150米, 80个点, 旋转1圈, 30度倾斜");
        
        TrajectoryPoint centerPoint = new TrajectoryPoint(39.9042, 116.4074, 50.0);
        List<TrajectoryPoint> ellipticalTrajectory = TrajectoryGeneratorUtil.generateEllipticalTrajectory(
            centerPoint, 300.0, 150.0, 80, 1, Math.toRadians(30));
        
        System.out.println("   生成轨迹点 (前10个):");
        for (int i = 0; i < Math.min(10, ellipticalTrajectory.size()); i++) {
            TrajectoryPoint point = ellipticalTrajectory.get(i);
            System.out.printf("     点%d: (%.6f, %.6f, %.2fm)\n", i, 
                             point.getLatitude(), point.getLongitude(), point.getAltitude());
        }
        
        System.out.println("   ... 总共生成了 " + ellipticalTrajectory.size() + " 个轨迹点\n");
    }
    
    /**
     * 演示综合轨迹示例
     */
    private static void demonstrateSampleTrajectory() {
        System.out.println("5. 综合轨迹示例:");
        System.out.println("   生成包含多种运动模式的轨迹");
        
        List<TrajectoryPoint> sampleTrajectory = TrajectoryGeneratorUtil.generateSampleTrajectory();
        
        System.out.println("   生成轨迹点 (前20个):");
        for (int i = 0; i < Math.min(20, sampleTrajectory.size()); i++) {
            TrajectoryPoint point = sampleTrajectory.get(i);
            System.out.printf("     点%d: (%.6f, %.6f, %.2fm)\n", i, 
                             point.getLatitude(), point.getLongitude(), point.getAltitude());
        }
        
        System.out.println("   ... 总共生成了 " + sampleTrajectory.size() + " 个轨迹点");
        System.out.println("   该轨迹包含随机游走和圆形运动模式\n");
    }
}