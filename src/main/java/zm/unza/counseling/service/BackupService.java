package zm.unza.counseling.service;

/**
 * Service interface for backup operations
 */
public interface BackupService {
    
    /**
     * Perform backup operation
     * @param backupType the type of backup (daily, weekly, monthly)
     */
    void performBackup(String backupType);
}