package com.programmer.escrow;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
@MapperScan(basePackages = "com.programmer.escrow", annotationClass = Mapper.class)
public class EscrowApplication {

    public static void main(String[] args) {
        SpringApplication.run(EscrowApplication.class, args);
    }
}
