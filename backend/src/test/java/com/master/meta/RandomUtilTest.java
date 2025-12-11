package com.master.meta;

import com.master.meta.utils.RandomUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class RandomUtilTest {
    private MockedStatic<Random> mockedRandom;
    @BeforeEach
    void setUp() {
        // 使用 Mockito-inline 对 Random 类进行静态方法 mock
        mockedRandom = Mockito.mockStatic(Random.class);
    }

    @AfterEach
    void tearDown() {
        // 清理 mock 对象，避免影响其他测试用例
        if (mockedRandom != null) {
            mockedRandom.close();
        }
    }
    @Test
    void testDoubleTypeString_NormalRange() {
        // 使用 MockedConstruction 来 mock Random 构造函数
        try (MockedConstruction<Random> mocked = Mockito.mockConstruction(Random.class,
                (mock, context) -> {
                    when(mock.nextDouble()).thenReturn(0.6);
                })) {

            String result = RandomUtil.generateRandomDoubleString(10, 20);
            assertEquals("16.00", result); // 10 + (20-10)*0.6 = 16.00
        }
    }

    @Test
    void test() {
        HashMap<String, Boolean> map = new HashMap<>();
        map.put("1", true);
        map.put("2", false);
        Map map2 = map;
        if (map2.containsKey(1)) {
            System.out.println("1");
        } else {
            System.out.println("2");
        }
    }
}
