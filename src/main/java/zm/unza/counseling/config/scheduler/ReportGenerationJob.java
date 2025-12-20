package zm.unza.counseling.config.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
public class ReportGenerationJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Logic to generate reports
    }
}