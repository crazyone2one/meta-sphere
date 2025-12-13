package com.master.meta;

import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
public class RandomUtilTest {
    private MockedStatic<Random> mockedRandom;
    private SensorUtil sensorUtil;

    @BeforeEach
    void setUp() {
        // 使用 Mockito-inline 对 Random 类进行静态方法 mock
        mockedRandom = Mockito.mockStatic(Random.class);
        // 初始化mock对象
        sensorUtil = Mockito.mock(SensorUtil.class);
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
    public List<Row> getWkkList(String tableName, Boolean deleted) {
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave2");
            Map<String, Object> map = new LinkedHashMap<>();
            if (Boolean.TRUE.equals(deleted)) {
                map.put("deleted", "0");
            }
            rows = Db.selectListByMap(tableName, map);
        } finally {
            DataSourceKey.clear();
        }
        return rows;
    }
    @Test
    void test() {
        // List<Row> sensorInRedis = sensorUtil.getWkkFromRedis("84493325060000156", WkkSensorEnum.GTXDY.getKey(), WkkSensorEnum.GTXDY.getTableName(), false);
        List<Row> sensorInRedis = getWkkList("sf_wkk_gtxdy_cddy", false);
        sensorInRedis.forEach(row -> System.out.println(row.getString("install_date")));
    }
}
