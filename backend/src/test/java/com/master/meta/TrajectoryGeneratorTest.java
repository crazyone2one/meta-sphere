package com.master.meta;

import com.master.meta.utils.TrajectoryGeneratorUtil;
import com.master.meta.utils.TrajectoryGeneratorUtil.TrajectoryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TrajectoryGeneratorTest {

    @Test
    public void testGenerateRandomWalkTrajectory() {
        // 创建起始点
        TrajectoryPoint startPoint = new TrajectoryPoint(39.9042, 116.4074, 50.0);
        
        // 生成随机游走轨迹
        List<TrajectoryPoint> trajectory = TrajectoryGeneratorUtil.generateRandomWalkTrajectory(
            startPoint, 100, 50.0, 0.3);
        
        // 验证轨迹点数量
        assertEquals(100, trajectory.size());
        
        // 验证起始点
        assertEquals(startPoint.getLatitude(), trajectory.get(0).getLatitude(), 0.00000001);
        assertEquals(startPoint.getLongitude(), trajectory.get(0).getLongitude(), 0.00000001);
        assertEquals(startPoint.getAltitude(), trajectory.get(0).getAltitude(), 0.01);
        
        // 输出前几个轨迹点用于验证
        System.out.println("随机游走轨迹示例:");
        for (int i = 0; i < Math.min(10, trajectory.size()); i++) {
            System.out.println("点 " + i + ": " + trajectory.get(i));
        }
    }
    
    @Test
    public void testGenerateCircularTrajectory() {
        // 创建中心点
        TrajectoryPoint centerPoint = new TrajectoryPoint(39.9042, 116.4074, 50.0);
        
        // 生成圆形轨迹
        List<TrajectoryPoint> circularTrajectory = TrajectoryGeneratorUtil.generateCircularTrajectory(
            centerPoint, 100.0, 50, 2); // 半径100米，50个点，转2圈
        
        // 验证轨迹点数量
        assertEquals(50, circularTrajectory.size());
        
        // 输出圆形轨迹点用于验证
        System.out.println("\n圆形轨迹示例:");
        for (int i = 0; i < Math.min(10, circularTrajectory.size()); i++) {
            System.out.println("圆上点 " + i + ": " + circularTrajectory.get(i));
        }
    }
    
    @Test
    public void testGenerateLinearTrajectory() {
        // 创建起始点和结束点
        TrajectoryPoint startPoint = new TrajectoryPoint(39.9042, 116.4074, 50.0);
        TrajectoryPoint endPoint = new TrajectoryPoint(39.9142, 116.4174, 60.0);
        
        // 生成线性轨迹
        List<TrajectoryPoint> linearTrajectory = TrajectoryGeneratorUtil.generateLinearTrajectory(
            startPoint, endPoint, 20);
        
        // 验证轨迹点数量
        assertEquals(20, linearTrajectory.size());
        
        // 验证起始点和结束点
        assertEquals(startPoint.getLatitude(), linearTrajectory.get(0).getLatitude(), 0.00000001);
        assertEquals(startPoint.getLongitude(), linearTrajectory.get(0).getLongitude(), 0.00000001);
        assertEquals(startPoint.getAltitude(), linearTrajectory.get(0).getAltitude(), 0.01);
        
        assertEquals(endPoint.getLatitude(), linearTrajectory.get(19).getLatitude(), 0.00000001);
        assertEquals(endPoint.getLongitude(), linearTrajectory.get(19).getLongitude(), 0.00000001);
        assertEquals(endPoint.getAltitude(), linearTrajectory.get(19).getAltitude(), 0.01);
        
        // 输出线性轨迹点用于验证
        System.out.println("\n线性轨迹示例:");
        for (int i = 0; i < linearTrajectory.size(); i += Math.max(1, linearTrajectory.size()/10)) {
            System.out.println("线性点 " + i + ": " + linearTrajectory.get(i));
        }
    }
    
    @Test
    public void testGenerateEllipticalTrajectory() {
        // 创建中心点
        TrajectoryPoint centerPoint = new TrajectoryPoint(39.9042, 116.4074, 50.0);
        
        // 生成椭圆轨迹
        List<TrajectoryPoint> ellipticalTrajectory = TrajectoryGeneratorUtil.generateEllipticalTrajectory(
            centerPoint, 200.0, 100.0, 60, 1, Math.toRadians(45)); // 长半轴200m，短半轴100m，转1圈，45度倾斜
        
        // 验证轨迹点数量
        assertEquals(60, ellipticalTrajectory.size());
        
        // 输出椭圆轨迹点用于验证
        System.out.println("\n椭圆轨迹示例:");
        for (int i = 0; i < Math.min(10, ellipticalTrajectory.size()); i++) {
            System.out.println("椭圆点 " + i + ": " + ellipticalTrajectory.get(i));
        }
    }
    
    @Test
    public void testGenerateSampleTrajectory() {
        // 生成样例轨迹
        List<TrajectoryPoint> sampleTrajectory = TrajectoryGeneratorUtil.generateSampleTrajectory();
        
        // 验证轨迹点数量（至少包含随机游走的50个点）
        assertTrue(sampleTrajectory.size() >= 50, "样本轨迹应至少包含50个点");
        
        // 输出样例轨迹点用于验证
        System.out.println("\n样例轨迹示例:");
        for (int i = 0; i < Math.min(20, sampleTrajectory.size()); i++) {
            System.out.println("样例点 " + i + ": " + sampleTrajectory.get(i));
        }
    }
    
    @Test
    public void testTrajectoryReasonableness() {
        // 创建起始点
        TrajectoryPoint startPoint = new TrajectoryPoint(39.9042, 116.4074, 50.0);
        
        // 生成随机游走轨迹
        List<TrajectoryPoint> trajectory = TrajectoryGeneratorUtil.generateRandomWalkTrajectory(
            startPoint, 50, 100.0, 0.5);
        
        // 检查轨迹的合理性：相邻点之间的距离不应过大
        boolean isReasonable = true;
        for (int i = 1; i < trajectory.size(); i++) {
            TrajectoryPoint prevPoint = trajectory.get(i-1);
            TrajectoryPoint currPoint = trajectory.get(i);
            
            // 计算两点间的近似距离（简单估算）
            double latDiff = Math.toRadians(currPoint.getLatitude() - prevPoint.getLatitude());
            double lonDiff = Math.toRadians(currPoint.getLongitude() - prevPoint.getLongitude());
            
            // 使用球面余弦定律计算距离
            double lat1Rad = Math.toRadians(prevPoint.getLatitude());
            double lat2Rad = Math.toRadians(currPoint.getLatitude());
            
            double a = Math.sin(latDiff/2) * Math.sin(latDiff/2) +
                       Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                       Math.sin(lonDiff/2) * Math.sin(lonDiff/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            double distance = 6378137.0 * c; // 地球半径 * 中心角
            
            // 检查距离是否在合理范围内（考虑到我们设定的最大步长是100米）
            if (distance > 200) { // 允许一定误差
                isReasonable = false;
                System.out.println("不合理的大距离: " + distance + " 米，从点 " + (i-1) + " 到点 " + i);
                break;
            }
        }
        
        assertTrue(isReasonable, "轨迹应显示合理的运动模式");
        
        System.out.println("\n轨迹合理性检查通过，连续点之间的距离都在合理范围内");
    }
}