package com.master.meta;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.master.meta.mapper")
public class MetaSphereApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetaSphereApplication.class, args);
    }

}
