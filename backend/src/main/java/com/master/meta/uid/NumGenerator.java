package com.master.meta.uid;

import com.master.meta.constants.ApplicationNumScope;
import jakarta.annotation.Resource;
import org.redisson.Redisson;
import org.redisson.api.RIdGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 2025/10/13
 */
@Component
public class NumGenerator {
    private static final long INIT = 100001L; // 代表从100001开始，各种domain的 num
    private static final long LIMIT = 1;

    private static Redisson redisson;
    private static StringRedisTemplate stringRedisTemplate;

    public static long nextNum(String prefix, ApplicationNumScope scope) {
        RIdGenerator idGenerator = redisson.getIdGenerator(prefix + "_" + scope.name());
        // 每次都尝试初始化，容量为1，只有一个线程可以初始化成功
        if (!idGenerator.isExists()) {
            idGenerator.tryInit(INIT, LIMIT);
        }
        return idGenerator.nextId();
    }

    public static long nextNum(ApplicationNumScope scope) {
        return nextNum(scope.name(), scope);
    }

    @Resource
    public void setRedisson(Redisson redisson) {
        NumGenerator.redisson = redisson;
    }
}
