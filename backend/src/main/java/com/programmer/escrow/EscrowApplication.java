package com.programmer.escrow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({
        "com.programmer.escrow.user.mapper",
        "com.programmer.escrow.demand.mapper",
        "com.programmer.escrow.quote.mapper",
        "com.programmer.escrow.order.mapper",
        "com.programmer.escrow.dispute.mapper",
        "com.programmer.escrow.admin.mapper"
})
public class EscrowApplication {

    public static void main(String[] args) {
        SpringApplication.run(EscrowApplication.class, args);
    }
}
