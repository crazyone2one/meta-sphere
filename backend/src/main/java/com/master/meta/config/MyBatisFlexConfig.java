package com.master.meta.config;

import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.dialect.DbType;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.mybatisflex.core.query.QueryColumnBehavior;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Configuration
public class MyBatisFlexConfig implements MyBatisFlexCustomizer {
    @Override
    public void customize(FlexGlobalConfig flexGlobalConfig) {
        flexGlobalConfig.setLogicDeleteColumn("deleted");
        FlexGlobalConfig.KeyConfig keyConfig = new FlexGlobalConfig.KeyConfig();
        keyConfig.setKeyType(KeyType.Generator);
        keyConfig.setValue(KeyGenerators.flexId);
        FlexGlobalConfig.getDefaultConfig().setKeyConfig(keyConfig);
        flexGlobalConfig.setDbType(DbType.MYSQL);

        // 如果传入的值是集合或数组，则使用 in 逻辑，否则使用 =（等于） 逻辑
        QueryColumnBehavior.setSmartConvertInToEquals(true);
        // 使用自定义规则忽略空集合参数
        // 当参数是空列表或数组时，不拼接查询条件
        QueryColumnBehavior.setIgnoreFunction((o) -> {
            // 1. 如果参数是 List 类型，判断是否为空（包括 null 或空集合）
            if (o instanceof List<?>) {
                return CollectionUtils.isEmpty((List<?>) o);
            }
            // 2. 其他类型：null、空字符串、纯空白字符串都忽略
            return o == null || "".equals(o)|| o.toString().trim().isEmpty();
        });
    }
}
