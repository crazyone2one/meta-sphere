package com.master.meta.utils;

/**
 * 2000国家大地坐标系(CGCS2000)坐标转换工具类
 * 
 * 2000国家大地坐标系参数：
 * - 长半轴 a = 6378137 m
 * - 扁率倒数 1/f = 298.257222101
 * - 第一偏心率平方 e² = 0.00669438002290
 * - 第二偏心率平方 e'² = 0.00673949675502
 */
public class CoordinateSystem2000Util {
    
    /**
     * CGCS2000椭球参数 - 长半轴(米)
     */
    public static final double CGCS2000_A = 6378137.0;
    
    /**
     * CGCS2000椭球参数 - 扁率倒数
     */
    public static final double CGCS2000_INVERSE_FLATTENING = 298.257222101;
    
    /**
     * CGCS2000椭球参数 - 第一偏心率平方
     */
    public static final double CGCS2000_E_SQUARED = 0.00669438002290;
    
    /**
     * CGCS2000椭球参数 - 第二偏心率平方
     */
    public static final double CGCS2000_EP_SQUARED = 0.00673949675502;
    
    /**
     * 弧度转角度
     */
    public static final double RAD_TO_DEG = 180.0 / Math.PI;
    
    /**
     * 角度转弧度
     */
    public static final double DEG_TO_RAD = Math.PI / 180.0;
    
    /**
     * 大地坐标转空间直角坐标
     * 
     * @param latitude 纬度(度)
     * @param longitude 经度(度)
     * @param height 椭球高(米)
     * @return 空间直角坐标数组 [X, Y, Z]
     */
    public static double[] geodeticToCartesian(double latitude, double longitude, double height) {
        double latRad = latitude * DEG_TO_RAD;
        double lonRad = longitude * DEG_TO_RAD;
        
        // 计算卯酉圈曲率半径 N
        double sinLat = Math.sin(latRad);
        double N = CGCS2000_A / Math.sqrt(1 - CGCS2000_E_SQUARED * sinLat * sinLat);
        
        // 计算空间直角坐标
        double X = (N + height) * Math.cos(latRad) * Math.cos(lonRad);
        double Y = (N + height) * Math.cos(latRad) * Math.sin(lonRad);
        double Z = (N * (1 - CGCS2000_E_SQUARED) + height) * sinLat;
        
        return new double[]{X, Y, Z};
    }
    
    /**
     * 空间直角坐标转大地坐标
     * 
     * @param X 空间直角坐标X
     * @param Y 空间直角坐标Y
     * @param Z 空间直角坐标Z
     * @return 大地坐标数组 [latitude, longitude, height]，单位为[度, 度, 米]
     */
    public static double[] cartesianToGeodetic(double X, double Y, double Z) {
        double p = Math.sqrt(X * X + Y * Y);
        double theta = Math.atan2(Z * CGCS2000_A, p * (CGCS2000_A * (1 - CGCS2000_E_SQUARED)));
        
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);
        
        double longitude = Math.atan2(Y, X);
        double latitude = Math.atan2(
            Z + CGCS2000_EP_SQUARED * CGCS2000_A * sinTheta * sinTheta * sinTheta,
            p - CGCS2000_E_SQUARED * CGCS2000_A * cosTheta * cosTheta * cosTheta
        );
        
        double sinLat = Math.sin(latitude);
        double N = CGCS2000_A / Math.sqrt(1 - CGCS2000_E_SQUARED * sinLat * sinLat);
        double height = p / Math.cos(latitude) - N;
        
        return new double[]{latitude * RAD_TO_DEG, longitude * RAD_TO_DEG, height};
    }
    
    /**
     * 大地坐标转高斯-克吕格投影坐标
     * 
     * @param latitude 纬度(度)
     * @param longitude 经度(度)
     * @param centralMeridian 中央子午线经度
     * @param falseEasting 东偏移(米)，默认500000米
     * @param falseNorthing 北偏移(米)，北半球为0，南半球通常为10000000米
     * @return 高斯-克吕格投影坐标 [Easting, Northing]
     */
    public static double[] geodeticToGaussKruger(double latitude, double longitude, double centralMeridian, 
                                                 double falseEasting, double falseNorthing) {
        double latRad = latitude * DEG_TO_RAD;
        double lonRad = longitude * DEG_TO_RAD;
        double centralMeridianRad = centralMeridian * DEG_TO_RAD;
        
        // 计算经差
        double dLon = lonRad - centralMeridianRad;
        
        // 计算卯酉圈曲率半径N和子午圈曲率半径M
        double sinLat = Math.sin(latRad);
        double cosLat = Math.cos(latRad);
        double sin2Lat = sinLat * sinLat;
        
        double N = CGCS2000_A / Math.sqrt(1 - CGCS2000_E_SQUARED * sin2Lat);
        double M = CGCS2000_A * (1 - CGCS2000_E_SQUARED) / 
                  Math.pow(1 - CGCS2000_E_SQUARED * sin2Lat, 1.5);
        
        // 计算辅助参数
        double t = Math.tan(latRad);
        double eta2 = CGCS2000_EP_SQUARED * cosLat * cosLat;
        
        // 计算高斯-克吕格投影坐标
        double x = M * latRad; // 子午线弧长
        
        // 使用泰勒级数展开计算
        x += N * sinLat * cosLat * dLon * dLon / 2.0;
        x += N * sinLat * Math.pow(cosLat, 3) * (5 - t * t + 9 * eta2 + 4 * eta2 * eta2) * 
             Math.pow(dLon, 4) / 24.0;
        x += N * sinLat * Math.pow(cosLat, 5) * 
             (61 - 58 * t * t + t * t * t * t) * Math.pow(dLon, 6) / 720.0;
        
        double y = N * cosLat * dLon;
        y += N * Math.pow(cosLat, 3) * (1 - t * t + eta2) * Math.pow(dLon, 3) / 6.0;
        y += N * Math.pow(cosLat, 5) * (5 - 18 * t * t + t * t * t * t - 14 * eta2 + 
              58 * t * t * eta2) * Math.pow(dLon, 5) / 120.0;
        
        return new double[]{y + falseEasting, x + falseNorthing};
    }
    
    /**
     * 大地坐标转高斯-克吕格投影坐标（6度带）
     * 
     * @param latitude 纬度(度)
     * @param longitude 经度(度)
     * @param zoneNumber 投影带号(1-60)
     * @return 高斯-克吕格投影坐标 [Easting, Northing]
     */
    public static double[] geodeticToGaussKruger6Degree(double latitude, double longitude, int zoneNumber) {
        double centralMeridian = zoneNumber * 6 - 3; // 6度带中央子午线
        return geodeticToGaussKruger(latitude, longitude, centralMeridian, 500000.0, 0.0);
    }
    
    /**
     * 大地坐标转高斯-克吕格投影坐标（3度带）
     * 
     * @param latitude 纬度(度)
     * @param longitude 经度(度)
     * @param zoneNumber 投影带号(1-120)
     * @return 高斯-克吕格投影坐标 [Easting, Northing]
     */
    public static double[] geodeticToGaussKruger3Degree(double latitude, double longitude, int zoneNumber) {
        double centralMeridian = zoneNumber * 3; // 3度带中央子午线
        return geodeticToGaussKruger(latitude, longitude, centralMeridian, 500000.0, 0.0);
    }
    
    /**
     * 高斯-克吕格投影坐标转大地坐标
     * 
     * @param easting 东坐标(米)
     * @param northing 北坐标(米)
     * @param centralMeridian 中央子午线经度
     * @param falseEasting 东偏移(米)，默认500000米
     * @param falseNorthing 北偏移(米)
     * @return 大地坐标 [latitude, longitude]，单位为度
     */
    public static double[] gaussKrugerToGeodetic(double easting, double northing, double centralMeridian,
                                                 double falseEasting, double falseNorthing) {
        // 计算相对于中央子午线的坐标
        double x = northing - falseNorthing;
        double y = easting - falseEasting;
        
        // 计算子午线弧长对应的纬度
        double latRad = x / CGCS2000_A; // 初始近似值
        
        // 迭代计算纬度
        int maxIterations = 10;
        for (int i = 0; i < maxIterations; i++) {
            double sinLat = Math.sin(latRad);
            double M = CGCS2000_A * (1 - CGCS2000_E_SQUARED) / 
                      Math.pow(1 - CGCS2000_E_SQUARED * sinLat * sinLat, 1.5);
            double latRadNew = latRad + (x - geodeticToMeridianArc(latRad)) / M;
            if (Math.abs(latRadNew - latRad) < 1e-12) {
                latRad = latRadNew;
                break;
            }
            latRad = latRadNew;
        }
        
        // 计算其他参数
        double sinLat = Math.sin(latRad);
        double cosLat = Math.cos(latRad);
        double t = Math.tan(latRad);
        double eta2 = CGCS2000_EP_SQUARED * cosLat * cosLat;
        
        double N = CGCS2000_A / Math.sqrt(1 - CGCS2000_E_SQUARED * sinLat * sinLat);
        
        // 计算经度差
        double dLon = y / (N * cosLat) - 
                     Math.pow(y, 3) * (1 - t * t + eta2) / (6 * Math.pow(N, 3) * Math.pow(cosLat, 3)) +
                     Math.pow(y, 5) * (5 - 18 * t * t + t * t * t * t + 14 * eta2 - 58 * t * t * eta2) / 
                     (120 * Math.pow(N, 5) * Math.pow(cosLat, 5));
        
        double latitude = latRad * RAD_TO_DEG;
        double longitude = (centralMeridian * DEG_TO_RAD + dLon) * RAD_TO_DEG;
        
        return new double[]{latitude, longitude};
    }
    
    /**
     * 计算子午线弧长
     */
    private static double geodeticToMeridianArc(double latRad) {
        double sinLat = Math.sin(latRad);
        double cosLat = Math.cos(latRad);
        
        // 使用泰勒级数展开计算子午线弧长
        double a = CGCS2000_A;
        double e2 = CGCS2000_E_SQUARED;
        double e4 = e2 * e2;
        double e6 = e4 * e2;
        double e8 = e4 * e4;

        return a * ((1 - e2/4 - 3*e4/64 - 5*e6/256 - 175*e8/16384) * latRad -
                (3*e2/8 + 3*e4/32 + 45*e6/1024 + 175*e8/8192) * Math.sin(2*latRad) +
                (15*e4/256 + 45*e6/1024 + 525*e8/16384) * Math.sin(4*latRad) -
                (35*e6/3072 + 175*e8/12288) * Math.sin(6*latRad) +
                (315*e8/131072) * Math.sin(8*latRad));
    }
    
    /**
     * WGS84坐标系转2000国家大地坐标系
     * 这是一个近似转换，实际应用中可能需要更精确的七参数转换
     * 
     * @param latitudeWGS84 WGS84纬度
     * @param longitudeWGS84 WGS84经度
     * @param heightWGS84 WGS84椭球高
     * @return CGCS2000坐标 [latitude, longitude, height]
     */
    public static double[] wgs84ToCGCS2000(double latitudeWGS84, double longitudeWGS84, double heightWGS84) {
        // 从WGS84大地坐标转换为WGS84空间直角坐标
        double[] wgs84xyz = geodeticToCartesianWGS84(latitudeWGS84, longitudeWGS84, heightWGS84);
        double x = wgs84xyz[0];
        double y = wgs84xyz[1];
        double z = wgs84xyz[2];
        
        // 简化的坐标转换（实际应用中可能需要七参数转换）
        // 这里使用近似方法，WGS84和CGCS2000差异很小

        return cartesianToGeodetic(x, y, z);
    }
    
    /**
     * WGS84椭球参数转换为大地坐标
     * WGS84椭球参数：a = 6378137.0, f = 1/298.257223563
     */
    private static double[] geodeticToCartesianWGS84(double latitude, double longitude, double height) {
        double a = 6378137.0; // WGS84长半轴
        double f = 1/298.257223563; // WGS84扁率
        double e2 = 2*f - f*f; // WGS84第一偏心率平方
        
        double latRad = latitude * DEG_TO_RAD;
        double lonRad = longitude * DEG_TO_RAD;
        
        double sinLat = Math.sin(latRad);
        double N = a / Math.sqrt(1 - e2 * sinLat * sinLat);
        
        double X = (N + height) * Math.cos(latRad) * Math.cos(lonRad);
        double Y = (N + height) * Math.cos(latRad) * Math.sin(lonRad);
        double Z = (N * (1 - e2) + height) * sinLat;
        
        return new double[]{X, Y, Z};
    }
}