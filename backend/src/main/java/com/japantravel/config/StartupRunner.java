package com.japantravel.config;

import com.japantravel.collector.WikipediaCollector;
import com.japantravel.repository.BulkRunRepository;
import com.japantravel.repository.CollectorRunRepository;
import com.japantravel.repository.DestinationRepository;
import com.japantravel.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class StartupRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final DestinationRepository destRepo;
    private final WikipediaCollector collector;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final CollectorRunRepository runs;
    private final BulkRunRepository bulkRuns;
    private final JdbcTemplate jdbc;
    private final boolean autoBootstrap;

    public StartupRunner(DestinationRepository destRepo, WikipediaCollector collector,
                         UserRepository users, PasswordEncoder encoder,
                         CollectorRunRepository runs, BulkRunRepository bulkRuns,
                         JdbcTemplate jdbc,
                         @Value("${collector.auto-bootstrap:false}") boolean autoBootstrap) {
        this.destRepo = destRepo; this.collector = collector;
        this.users = users; this.encoder = encoder;
        this.runs = runs; this.bulkRuns = bulkRuns;
        this.jdbc = jdbc;
        this.autoBootstrap = autoBootstrap;
    }

    @Override
    public void run(String... args) {
        File dataDir = new File(System.getProperty("user.dir"), "../data");
        if (!dataDir.exists() && dataDir.mkdirs()) {
            log.info("data 디렉토리 생성: {}", dataDir.getAbsolutePath());
        }

        ensureCollectorRunsColumns();

        seedAdminIfMissing();

        int zombies = runs.cleanupZombies();
        if (zombies > 0) log.info("이전 세션에서 남은 collector_runs {}건을 aborted로 정리했습니다.", zombies);
        int bulkZombies = bulkRuns.cleanupZombies();
        if (bulkZombies > 0) log.info("이전 세션에서 남은 bulk_runs {}건을 aborted로 정리했습니다.", bulkZombies);

        int existing = destRepo.count();
        log.info("부팅 시 destinations 행 수: {}", existing);
        if (autoBootstrap && existing == 0) {
            log.info("auto-bootstrap=true 이고 DB가 비어 있어 도쿄도 관광지 수집을 시작합니다.");
            new Thread(() -> collector.runBlocking("destination", "도쿄도")).start();
        }
    }

    private void ensureCollectorRunsColumns() {
        try {
            java.util.Set<String> cols = new java.util.HashSet<>();
            jdbc.query("PRAGMA table_info(collector_runs)", rs -> { cols.add(rs.getString("name")); });
            if (cols.isEmpty()) return;
            if (!cols.contains("bulk_run_id")) {
                jdbc.execute("ALTER TABLE collector_runs ADD COLUMN bulk_run_id INTEGER REFERENCES bulk_runs(id)");
                jdbc.execute("CREATE INDEX IF NOT EXISTS idx_runs_bulk ON collector_runs(bulk_run_id)");
                log.info("collector_runs.bulk_run_id 컬럼 추가 (스키마 자동 마이그레이션)");
            }
            if (!cols.contains("last_heartbeat_at")) {
                jdbc.execute("ALTER TABLE collector_runs ADD COLUMN last_heartbeat_at TEXT");
                jdbc.execute("CREATE INDEX IF NOT EXISTS idx_runs_heartbeat ON collector_runs(status, last_heartbeat_at)");
                log.info("collector_runs.last_heartbeat_at 컬럼 추가 (스키마 자동 마이그레이션)");
            }
            if (!cols.contains("stage")) {
                jdbc.execute("ALTER TABLE collector_runs ADD COLUMN stage TEXT");
                log.info("collector_runs.stage 컬럼 추가 (스키마 자동 마이그레이션)");
            }
        } catch (Exception e) {
            log.warn("collector_runs 스키마 점검 실패: {}", e.getMessage());
        }
    }

    private void seedAdminIfMissing() {
        if (users.existsByUsername("admin")) return;
        try {
            long id = users.insertWithId(1L, "admin", encoder.encode("admin1234"), "관리자", "ADMIN");
            log.info("admin 계정을 시드했습니다 (id={}, username=admin, password=admin1234)", id);
        } catch (Exception e) {
            log.warn("admin 시드 실패 — 이미 id=1 사용자가 존재할 수 있음: {}", e.getMessage());
        }
    }
}
