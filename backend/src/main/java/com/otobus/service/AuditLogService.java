package com.otobus.service;

import com.otobus.entity.AuditLog;
import com.otobus.entity.User;
import com.otobus.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * İşlem loglama servisi.
 * Sistemdeki tüm önemli olayları kayıt altına alır.
 * Admin panelinde denetim geçmişi olarak görüntülenir.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Yeni bir audit log kaydı oluşturur.
     */
    public void log(User user, String action, String entityType, Long entityId, String details, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }

    /**
     * Kullanıcı bilgisi olmadan log kaydı (örn: başarısız giriş denemesi).
     */
    public void logAnonymous(String action, String entityType, String details, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    /**
     * Tüm logları temizler (sadece ADMIN kullanabilir).
     */
    public void clearAllLogs() {
        auditLogRepository.deleteAll();
    }
}
