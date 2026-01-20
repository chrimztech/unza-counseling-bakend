package zm.unza.counseling.service.impl;

import zm.unza.counseling.service.BackupService;
import org.springframework.stereotype.Service;

/**
 * Implementation of BackupService
 */
@Service
public class BackupServiceImpl implements BackupService {

    @Override
    public void performBackup(String backupType) {
        // Implementation for performing backup operations
        // This would backup database and important files based on the backup type
    }
}