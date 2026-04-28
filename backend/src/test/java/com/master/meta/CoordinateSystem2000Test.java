package com.master.meta;

import com.master.meta.utils.CoordinateSystem2000Util;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CoordinateSystem2000Test {
    
    @Test
    public void testGeodeticToCartesian() {
        // 测试将大地坐标转换为空间直角坐标
        double latitude = 39.9042; // 北京大致纬度
        double longitude = 116.4074; // 北京大致经度
        double height = 100.0; // 高度100米
        
        double[] xyz = CoordinateSystem2000Util.geodeticToCartesian(latitude, longitude, height);
        
        System.out.println("大地坐标 (" + latitude + ", " + longitude + ", " + height + ")");
        System.out.println("空间直角坐标 (" + xyz[0] + ", " + xyz[1] + ", " + xyz[2] + ")");
        
        // 验证转换结果的合理性
        assertNotNull(xyz);
        assertEquals(3, xyz.length);
        assertTrue(xyz[0] > 0); // X坐标应为正值（大致在东半球）
        assertTrue(xyz[1] > 0); // Y坐标应为正值（大致在北半球）
    }
    
    @Test
    public void testCartesianToGeodetic() {
        // 测试将空间直角坐标转换为大地坐标
        double X = 1232900.0; // 示例X坐标
        double Y = 4364000.0; // 示例Y坐标
        double Z = 4080000.0; // 示例Z坐标
        
        double[] llh = CoordinateSystem2000Util.cartesianToGeodetic(X, Y, Z);
        
        System.out.println("空间直角坐标 (" + X + ", " + Y + ", " + Z + ")");
        System.out.println("大地坐标 (" + llh[0] + ", " + llh[1] + ", " + llh[2] + ")");
        
        // 验证转换结果的合理性
        assertNotNull(llh);
        assertEquals(3, llh.length);
    }
    
    @Test
    public void testGeodeticToGaussKruger6Degree() {
        // 测试将大地坐标转换为6度带高斯-克吕格投影坐标
        double latitude = 39.9042; // 北京大致纬度
        double longitude = 116.4074; // 北京大致经度
        
        // 计算该经度所在的6度带号
        int zoneNumber = (int) Math.floor((longitude + 6) / 6) + 1; // 例如，116.4074度经度在第20带
        
        double[] xy = CoordinateSystem2000Util.geodeticToGaussKruger6Degree(latitude, longitude, zoneNumber);
        
        System.out.println("大地坐标 (" + latitude + ", " + longitude + ") in zone " + zoneNumber);
        System.out.println("高斯-克吕格投影坐标 (" + xy[0] + ", " + xy[1] + ")");
        
        // 验证转换结果的合理性
        assertNotNull(xy);
        assertEquals(2, xy.length);
        assertTrue(xy[0] > 0); // 东坐标通常为正值
        assertTrue(xy[1] > 0); // 北坐标通常为正值
    }
    
    @Test
    public void testGeodeticToGaussKruger3Degree() {
        // 测试将大地坐标转换为3度带高斯-克吕格投影坐标
        double latitude = 39.9042; // 北京大致纬度
        double longitude = 116.4074; // 北京大致经度
        
        // 计算该经度所在的3度带号
        int zoneNumber = (int) Math.floor(longitude / 3) + 1; // 例如，116.4074度经度在第39带
        
        double[] xy = CoordinateSystem2000Util.geodeticToGaussKruger3Degree(latitude, longitude, zoneNumber);
        
        System.out.println("大地坐标 (" + latitude + ", " + longitude + ") in 3-degree zone " + zoneNumber);
        System.out.println("高斯-克吕格投影坐标 (" + xy[0] + ", " + xy[1] + ")");
        
        // 验证转换结果的合理性
        assertNotNull(xy);
        assertEquals(2, xy.length);
        assertTrue(xy[0] > 0); // 东坐标通常为正值
        assertTrue(xy[1] > 0); // 北坐标通常为正值
    }
    
    @Test
    public void testGaussKrugerToGeodetic() {
        // 测试将高斯-克吕格投影坐标转换为大地坐标
        double easting = 500000.0; // 中央子午线上的东坐标
        double northing = 4418000.0; // 示例北坐标
        double centralMeridian = 117.0; // 中央子午线经度
        
        double[] ll = CoordinateSystem2000Util.gaussKrugerToGeodetic(easting, northing, centralMeridian, 
                                                                    500000.0, 0.0);
        
        System.out.println("高斯-克吕格投影坐标 (" + easting + ", " + northing + ") with central meridian " + centralMeridian);
        System.out.println("大地坐标 (" + ll[0] + ", " + ll[1] + ")");
        
        // 验证转换结果的合理性
        assertNotNull(ll);
        assertEquals(2, ll.length);
    }
    
    @Test
    public void testCoordinateRoundTrip() {
        // 测试坐标转换的往返精度
        double latitude = 39.9042;
        double longitude = 116.4074;
        double height = 100.0;
        
        // 大地坐标 -> 空间直角坐标 -> 大地坐标
        double[] xyz = CoordinateSystem2000Util.geodeticToCartesian(latitude, longitude, height);
        double[] result = CoordinateSystem2000Util.cartesianToGeodetic(xyz[0], xyz[1], xyz[2]);
        
        System.out.println("Original: (" + latitude + ", " + longitude + ", " + height + ")");
        System.out.println("Round trip: (" + result[0] + ", " + result[1] + ", " + result[2] + ")");
        
        // 验证往返转换的精度（误差应该很小）
        assertEquals(latitude, result[0], 1e-8, "Latitude should match after round trip");
        assertEquals(longitude, result[1], 1e-8, "Longitude should match after round trip");
        assertEquals(height, result[2], 1e-3, "Height should match after round trip");
    }
    
    @Test
    public void testCGCS2000Constants() {
        // 验证CGCS2000椭球参数
        assertEquals(6378137.0, CoordinateSystem2000Util.CGCS2000_A, 0.0, "CGCS2000 semi-major axis should be 6378137.0");
        assertEquals(298.257222101, CoordinateSystem2000Util.CGCS2000_INVERSE_FLATTENING, 0.0, "CGCS2000 inverse flattening should match");
        assertEquals(0.00669438002290, CoordinateSystem2000Util.CGCS2000_E_SQUARED, 1e-12, "CGCS2000 first eccentricity squared should match");
        assertEquals(0.00673949675502, CoordinateSystem2000Util.CGCS2000_EP_SQUARED, 1e-12, "CGCS2000 second eccentricity squared should match");
    }
}