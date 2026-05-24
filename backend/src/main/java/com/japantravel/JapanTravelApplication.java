package com.japantravel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class JapanTravelApplication {
    public static void main(String[] args) {
        // SQLite는 부모 디렉토리를 자동 생성하지 않으므로 사전 보장.
        java.io.File dataDir = new java.io.File(System.getProperty("user.dir"), "../data");
        dataDir.mkdirs();
        SpringApplication.run(JapanTravelApplication.class, args);
    }
}
