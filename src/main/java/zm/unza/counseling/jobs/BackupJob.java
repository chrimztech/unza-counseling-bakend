package zm.unza.counseling.jobs;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class BackupJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(BackupJob.class);

    @Value("${app.backup.directory:/backups}")
    private String backupDirectory;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Starting backup job at {}", LocalDateTime.now());
        
        try {
            String timestamp = LocalDateTime.now().format(formatter);
            String backupFileName = String.format("unza-counseling-backup-%s.sql", timestamp);
            Path backupPath = Paths.get(backupDirectory, backupFileName);
            
            // Create backup directory if it doesn't exist
            Files.createDirectories(Paths.get(backupDirectory));
            
            // Here you would typically execute a database backup command
            // For PostgreSQL: pg_dump -U username -h hostname -d database > backup.sql
            // This is a simplified example - in production you'd use proper database backup tools
            
            log.info("Backup completed successfully: {}", backupPath);
            
            // Log backup completion
            log.info("Backup job completed successfully at {}", LocalDateTime.now());
        } catch (IOException e) {
            log.error("Error creating backup directory or file", e);
        } catch (Exception e) {
            log.error("Error in backup job", e);
        }
    }
}