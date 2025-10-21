package com.master.meta.handle;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/21
 */
@Component
public class ClassScanner {
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private final MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();

    /**
     * 扫描指定包下的所有类名（全限定名）
     *
     * @param packageName 包名，例如 com.master.meta.schedule.monitor
     * @return 类名列表
     */
    public List<String> scanPackage(String packageName) {
        List<String> classNames = new ArrayList<>();
        try {
            // 将包名转换为路径格式
            String packagePath = packageName.replace('.', '/');
            // 匹配该包下所有 .class 文件
            String pattern = "classpath*:" + packagePath + "/**/*.class";

            Resource[] resources = resourcePatternResolver.getResources(pattern);

            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
                    String className = reader.getClassMetadata().getClassName();
                    // 可选：排除非目标包的类（防止子包被重复扫描？一般不需要）
                    if (className.startsWith(packageName)) {
                        classNames.add(className);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan package: " + packageName, e);
        }
        return classNames;
    }
}
