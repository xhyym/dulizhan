package com.indiestation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 独立站后台管理系统启动类
 *
 * @author IndieStation
 */
@SpringBootApplication
@MapperScan("com.indiestation.mapper")
@EnableAsync
@EnableScheduling
public class IndieStationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndieStationApplication.class, args);
        System.out.println("============================================");
        System.out.println("  IndieStation Admin Boot 启动成功!");
        System.out.println("  http://localhost:8080");
        System.out.println("============================================");
    }
}
