package com.master.meta;

import com.master.meta.utils.RandomUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试人员信息生成方法
 */
@SpringBootTest
public class PersonInfoGenerationTest {

    @Test
    public void testGeneratePersonInfo() {
        String generateIdNumber = RandomUtil.generateIdNumber();
        // 生成人员信息
        String personInfo = RandomUtil.generatePersonInfo(generateIdNumber);
        
        // 验证返回值不为空
        assertNotNull(personInfo);
        assertFalse(personInfo.isEmpty());
        
        // 验证信息被正确分隔成13个字段
        String[] fields = personInfo.split(";");
        assertEquals(13, fields.length, "人员信息应该包含13个字段");
        
        // 验证各个字段
        String name = fields[0];
        String idNumber = fields[1];
        String jobTitle = fields[2];
        String department = fields[3];
        String isInternal = fields[4];
        String gender = fields[5];
        String education = fields[6];
        String maritalStatus = fields[7];
        String phoneNumber = fields[8];
        String address = fields[9];
        String certificate = fields[10];
        String certificateDate = fields[11];
        
        // 验证姓名不为空
        assertFalse(name.isEmpty(), "姓名不应为空");
        
        // 验证身份证号格式（18位）
        assertEquals(18, idNumber.length(), "身份证号应为18位");
        
        // 验证性别为男或女
        assertTrue(gender.equals("男") || gender.equals("女"), "性别应为男或女");
        
        // 验证是否本单位人员为是或否
        assertTrue(isInternal.equals("是") || isInternal.equals("否"), "是否本单位人员应为是或否");
        
        // 验证手机号格式（11位）
        assertEquals(11, phoneNumber.length(), "手机号应为11位");
        assertTrue(phoneNumber.startsWith("1"), "手机号应以1开头");
        
        // 验证身份证号中的性别位与实际性别一致
        char genderDigit = idNumber.charAt(16);
        String expectedGender = (genderDigit % 2 == 0) ? "女" : "男";
        assertEquals(expectedGender, gender, "身份证号中的性别位与实际性别应一致");
        
        System.out.println("生成的人员信息: " + personInfo);
    }
    
    @Test
    public void testGenerateMultiplePersonInfos() {
        // 生成多个人员信息，验证每个都符合格式
        for (int i = 0; i < 10; i++) {
            String generateIdNumber = RandomUtil.generateIdNumber();
            String personInfo = RandomUtil.generatePersonInfo(generateIdNumber);
            String[] fields = personInfo.split(";");
            
            assertEquals(13, fields.length, "人员信息应该包含13个字段，第" + (i+1) + "次生成");
            
            // 验证身份证号格式
            String idNumber = fields[1];
            assertEquals(18, idNumber.length(), "身份证号应为18位，第" + (i+1) + "次生成");
            
            // 验证手机号格式
            String phoneNumber = fields[8];
            assertEquals(11, phoneNumber.length(), "手机号应为11位，第" + (i+1) + "次生成");
            
            // 验证性别一致性
            char genderDigit = idNumber.charAt(16);
            String gender = fields[5];
            String expectedGender = (genderDigit % 2 == 0) ? "女" : "男";
            assertEquals(expectedGender, gender, "身份证号中的性别位与实际性别应一致，第" + (i+1) + "次生成");
        }
    }
}