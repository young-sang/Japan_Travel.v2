package com.japantravel.config;

import com.japantravel.collector.WikipediaCollector;
import com.japantravel.repository.DestinationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class StartupRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final DestinationRepository destRepo;
    private final WikipediaCollector collector;
    private final boolean autoBootstrap;

    public StartupRunner(DestinationRepository destRepo, WikipediaCollector collector,
                         @Value("${collector.auto-bootstrap:false}") boolean autoBootstrap) {
        this.destRepo = destRepo; this.collector = collector; this.autoBootstrap = autoBootstrap;
    }

    @Override
    public void run(String... args) {
        File dataDir = new File(System.getProperty("user.dir"), "../data");
        if (!dataDir.exists() && dataDir.mkdirs()) {
            log.info("data 디렉토리 생성: {}", dataDir.getAbsolutePath());
        }

        int existing = destRepo.count();
        log.info("부팅 시 destinations 행 수: {}", existing);
        if (autoBootstrap && existing == 0) {
            log.info("auto-bootstrap=true 이고 DB가 비어 있어 도쿄도 관광지 수집을 시작합니다.");
            new Thread(() -> collector.runBlocking("destination", "도쿄도")).start();
        }
    }
}
