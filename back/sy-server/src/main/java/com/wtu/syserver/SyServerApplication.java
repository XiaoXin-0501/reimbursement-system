package com.wtu.syserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wtu.syserver.mapper")
public class SyServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyServerApplication.class, args);
    }

}
