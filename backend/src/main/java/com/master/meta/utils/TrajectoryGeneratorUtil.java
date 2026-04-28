package com.master.meta.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 2000国家大地坐标系运动轨迹生成工具类
 * 
 * 用于随机生成符合实际运动规律的轨迹坐标序列
 */
public class TrajectoryGeneratorUtil {
    
    private static final Random random = new Random();
    
    /**
     * 轨迹点数据结构
     */
    public static class TrajectoryPoint {
        private double latitude;   // 纬度
        private double longitude;  // 经度
        private double altitude;   // 高程
        
        // 无参构造函数，用于JSON反序列化
        public TrajectoryPoint() {
        }
        
        public TrajectoryPoint(double latitude, double longitude, double altitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.altitude = altitude;
        }
        
        // Getter方法
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public double getAltitude() { return altitude; }
        
        // Setter方法，用于JSON反序列化
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        public void setAltitude(double altitude) { this.altitude = altitude; }
        
        @Override
        public String toString() {
            return String.format("%.8f,%.8f,%.2f", latitude, longitude, altitude);
        }
    }
    
    /**
     * 生成随机运动轨迹
     * 
     * @param startPoint 起始点坐标
     * @param numPoints 生成轨迹点的数量
     * @param maxStepDistance 最大步长(单位: 米)
     * @param maxAltitudeChange 最大高度变化(单位: 米)
     * @return 轨迹点列表
     */
    public static List<TrajectoryPoint> generateTrajectory(TrajectoryPoint startPoint, 
                                                          int numPoints, 
                                                          double maxStepDistance, 
                                                          double maxAltitudeChange) {
        List<TrajectoryPoint> trajectory = new ArrayList<>();
        trajectory.add(startPoint);
        
        TrajectoryPoint currentPoint = startPoint;
        
        for (int i = 1; i < numPoints; i++) {
            TrajectoryPoint nextPoint = generateNextPoint(currentPoint, maxStepDistance, maxAltitudeChange);
            trajectory.add(nextPoint);
            currentPoint = nextPoint;
        }
        
        return trajectory;
    }
    
    /**
     * 生成下一个轨迹点
     */
    private static TrajectoryPoint generateNextPoint(TrajectoryPoint currentPoint, 
                                                   double maxStepDistance, 
                                                   double maxAltitudeChange) {
        // 随机生成移动方向(角度)
        double direction = random.nextDouble() * 2 * Math.PI; // 0到2π弧度
        
        // 随机生成移动距离(不超过最大步长)
        double distance = random.nextDouble() * maxStepDistance;
        
        // 随机生成高度变化(不超过最大高度变化)
        double altitudeChange = (random.nextDouble() - 0.5) * 2 * maxAltitudeChange;
        
        // 计算新的经纬度坐标
        double[] newCoord = calculateNewCoordinate(currentPoint.getLatitude(), 
                                                  currentPoint.getLongitude(), 
                                                  distance, 
                                                  direction);
        
        double newLatitude = newCoord[0];
        double newLongitude = newCoord[1];
        double newAltitude = currentPoint.getAltitude() + altitudeChange;
        
        return new TrajectoryPoint(newLatitude, newLongitude, newAltitude);
    }
    
    /**
     * 根据当前位置、移动距离和方向计算新的地理坐标
     * 
     * @param lat 当前纬度
     * @param lon 当前经度
     * @param distance 移动距离(米)
     * @param direction 移动方向(弧度，0为正北，顺时针增加)
     * @return 新的经纬度坐标数组 [纬度, 经度]
     */
    private static double[] calculateNewCoordinate(double lat, double lon, double distance, double direction) {
        // 将角度转换为弧度
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        
        // 计算在球面上移动后的新坐标
        // 使用球面三角学公式
        double earthRadius = 6378137.0; // CGCS2000椭球长半轴
        double angularDistance = distance / earthRadius;
        
        // 计算新纬度
        double newLatRad = Math.asin(
            Math.sin(latRad) * Math.cos(angularDistance) + 
            Math.cos(latRad) * Math.sin(angularDistance) * Math.cos(direction)
        );
        
        // 计算新经度
        double newLonRad = lonRad + Math.atan2(
            Math.sin(direction) * Math.sin(angularDistance) * Math.cos(latRad),
            Math.cos(angularDistance) - Math.sin(latRad) * Math.sin(newLatRad)
        );
        
        // 将弧度转换回度
        double newLat = Math.toDegrees(newLatRad);
        double newLon = Math.toDegrees(newLonRad);
        
        return new double[]{newLat, newLon};
    }
    
    /**
     * 生成线性运动轨迹（匀速直线运动）
     * 
     * @param startPoint 起始点
     * @param endPoint 结束点
     * @param numPoints 轨迹点总数
     * @return 线性轨迹点列表
     */
    public static List<TrajectoryPoint> generateLinearTrajectory(TrajectoryPoint startPoint, 
                                                               TrajectoryPoint endPoint, 
                                                               int numPoints) {
        List<TrajectoryPoint> trajectory = new ArrayList<>();
        
        double latDiff = endPoint.getLatitude() - startPoint.getLatitude();
        double lonDiff = endPoint.getLongitude() - startPoint.getLongitude();
        double altDiff = endPoint.getAltitude() - startPoint.getAltitude();
        
        for (int i = 0; i < numPoints; i++) {
            double ratio = (double) i / (numPoints - 1);
            
            double lat = startPoint.getLatitude() + latDiff * ratio;
            double lon = startPoint.getLongitude() + lonDiff * ratio;
            double alt = startPoint.getAltitude() + altDiff * ratio;
            
            trajectory.add(new TrajectoryPoint(lat, lon, alt));
        }
        
        return trajectory;
    }
    
    /**
     * 生成圆形运动轨迹
     * 
     * @param centerPoint 中心点坐标
     * @param radius 圆半径(米)
     * @param numPoints 轨迹点总数
     * @param revolutions 旋转圈数
     * @return 圆形轨迹点列表
     */
    public static List<TrajectoryPoint> generateCircularTrajectory(TrajectoryPoint centerPoint, 
                                                                 double radius, 
                                                                 int numPoints, 
                                                                 int revolutions) {
        List<TrajectoryPoint> trajectory = new ArrayList<>();
        
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i * revolutions / numPoints;
            
            // 计算圆上点的坐标
            double[] coord = calculateNewCoordinate(centerPoint.getLatitude(), 
                                                  centerPoint.getLongitude(), 
                                                  radius, 
                                                  angle);
            
            double lat = coord[0];
            double lon = coord[1];
            double alt = centerPoint.getAltitude(); // 圆形轨迹保持高度不变
            
            trajectory.add(new TrajectoryPoint(lat, lon, alt));
        }
        
        return trajectory;
    }
    
    /**
     * 生成椭圆运动轨迹
     * 
     * @param centerPoint 中心点坐标
     * @param semiMajorAxis 长半轴(米)
     * @param semiMinorAxis 短半轴(米)
     * @param numPoints 轨迹点总数
     * @param revolutions 旋转圈数
     * @param orientation 椭圆长轴方向(弧度)
     * @return 椭圆轨迹点列表
     */
    public static List<TrajectoryPoint> generateEllipticalTrajectory(TrajectoryPoint centerPoint, 
                                                                   double semiMajorAxis, 
                                                                   double semiMinorAxis, 
                                                                   int numPoints, 
                                                                   int revolutions,
                                                                   double orientation) {
        List<TrajectoryPoint> trajectory = new ArrayList<>();
        
        for (int i = 0; i < numPoints; i++) {
            double parametricAngle = 2 * Math.PI * i * revolutions / numPoints;
            
            // 椭圆参数方程
            double x = semiMajorAxis * Math.cos(parametricAngle);
            double y = semiMinorAxis * Math.sin(parametricAngle);
            
            // 应用旋转
            double rotatedX = x * Math.cos(orientation) - y * Math.sin(orientation);
            double rotatedY = x * Math.sin(orientation) + y * Math.cos(orientation);
            
            // 计算方位角和距离
            double distance = Math.sqrt(rotatedX * rotatedX + rotatedY * rotatedY);
            double direction = Math.atan2(rotatedY, rotatedX);
            
            // 计算地理坐标
            double[] coord = calculateNewCoordinate(centerPoint.getLatitude(), 
                                                  centerPoint.getLongitude(), 
                                                  distance, 
                                                  direction);
            
            double lat = coord[0];
            double lon = coord[1];
            double alt = centerPoint.getAltitude();
            
            trajectory.add(new TrajectoryPoint(lat, lon, alt));
        }
        
        return trajectory;
    }
    
    /**
     * 生成随机游走轨迹（更自然的运动模式）
     * 
     * @param startPoint 起始点
     * @param numPoints 轨迹点数量
     * @param stepSize 基础步长(米)
     * @param randomness 随机性系数(0-1, 0表示直线运动，1表示完全随机)
     * @return 随机游走轨迹点列表
     */
    public static List<TrajectoryPoint> generateRandomWalkTrajectory(TrajectoryPoint startPoint, 
                                                                   int numPoints, 
                                                                   double stepSize, 
                                                                   double randomness) {
        List<TrajectoryPoint> trajectory = new ArrayList<>();
        trajectory.add(startPoint);
        
        TrajectoryPoint currentPoint = startPoint;
        double lastDirection = random.nextDouble() * 2 * Math.PI; // 初始方向
        
        for (int i = 1; i < numPoints; i++) {
            // 基于上一步方向，添加随机偏移来决定当前方向
            double directionVariation = (random.nextDouble() - 0.5) * 2 * Math.PI * randomness;
            double currentDirection = lastDirection + directionVariation;
            
            // 标准化方向到 0-2π 范围
            currentDirection = ((currentDirection % (2 * Math.PI)) + 2 * Math.PI) % (2 * Math.PI);
            
            // 随机生成移动距离
            double currentStepSize = stepSize * (0.5 + random.nextDouble() * 0.5); // ±50% 变化
            
            // 计算新坐标
            double[] newCoord = calculateNewCoordinate(currentPoint.getLatitude(), 
                                                      currentPoint.getLongitude(), 
                                                      currentStepSize, 
                                                      currentDirection);
            
            // 随机高度变化
            double altitudeChange = (random.nextDouble() - 0.5) * 10; // ±5米高度变化
            double newAltitude = currentPoint.getAltitude() + altitudeChange;
            
            TrajectoryPoint newPoint = new TrajectoryPoint(newCoord[0], newCoord[1], newAltitude);
            trajectory.add(newPoint);
            
            currentPoint = newPoint;
            lastDirection = currentDirection;
        }
        
        return trajectory;
    }
    
    /**
     * 生成样例轨迹数据（北京地区附近）
     */
    public static List<TrajectoryPoint> generateSampleTrajectory() {
        // 设置起始点（北京附近）
        TrajectoryPoint startPoint = new TrajectoryPoint(39.9042, 116.4074, 50.0);
        
        // 生成包含多种运动模式的轨迹
        List<TrajectoryPoint> trajectory = new ArrayList<>();
        
        // 添加一些随机游走轨迹点
        trajectory.addAll(generateRandomWalkTrajectory(startPoint, 50, 100, 0.3));
        
        // 获取当前轨迹的最后一个点作为新轨迹的起点
        TrajectoryPoint lastPoint = trajectory.get(trajectory.size() - 1);
        
        // 添加一些圆形轨迹点
        trajectory.addAll(generateCircularTrajectory(lastPoint, 200, 100, 1));
        
        return trajectory;
    }
}