package com.japantravel.service;

import com.japantravel.repository.AuditLogRepository;
import com.japantravel.security.AppUserPrincipal;
import com.japantravel.security.CurrentUser;
import org.springframework.stereotype.Service;

/**
 * 명시적 호출 방식 감사 로그.
 * 로깅 실패가 비즈니스 흐름을 막지 않도록 모든 호출은 try/catch로 감싼다.
 */
@Service
public class AuditService {

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) { this.repo = repo; }

    public void log(Long userId, String username, String action,
                    String targetType, Long targetId, String detail) {
        try {
            repo.insert(userId, username, action, targetType, targetId, detail);
        } catch (Exception ignored) {
            // 로깅 실패는 비즈니스에 영향 없도록 무시 (출력도 안 함)
        }
    }

    public void log(CurrentUser cu, String action, String targetType, Long targetId, String detail) {
        AppUserPrincipal p = cu == null ? null : cu.principalOrNull();
        Long uid = p == null ? null : p.getId();
        String uname = p == null ? null : p.getUsername();
        log(uid, uname, action, targetType, targetId, detail);
    }
}
