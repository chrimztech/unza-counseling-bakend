package zm.unza.counseling.config.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import zm.unza.counseling.jobs.*;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class QuartzConfig {

    @Bean
    public JobDetail appointmentReminderJobDetail() {
        return JobBuilder.newJob(AppointmentReminderJob.class)
                .withIdentity("appointmentReminderJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger appointmentReminderTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(appointmentReminderJobDetail())
                .withIdentity("appointmentReminderTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(9, 0)) // Run daily at 9 AM
                .build();
    }

    @Bean
    public JobDetail riskAssessmentAlertJobDetail() {
        return JobBuilder.newJob(RiskAssessmentAlertJob.class)
                .withIdentity("riskAssessmentAlertJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger riskAssessmentAlertTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(riskAssessmentAlertJobDetail())
                .withIdentity("riskAssessmentAlertTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(8, 30)) // Run daily at 8:30 AM
                .build();
    }

    @Bean
    public JobDetail dataCleanupJobDetail() {
        return JobBuilder.newJob(DataCleanupJob.class)
                .withIdentity("dataCleanupJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger dataCleanupTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(dataCleanupJobDetail())
                .withIdentity("dataCleanupTrigger")
                .withSchedule(CronScheduleBuilder.weeklyOnDayAndHourAndMinute(1, 2, 0)) // Run weekly on Sunday at 2 AM
                .build();
    }

    @Bean
    public JobDetail backupJobDetail() {
        return JobBuilder.newJob(BackupJob.class)
                .withIdentity("backupJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger backupTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(backupJobDetail())
                .withIdentity("backupTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(3, 0)) // Run daily at 3 AM
                .build();
    }

    @Bean
    public JobDetail reportGenerationJobDetail() {
        return JobBuilder.newJob(ReportGenerationJob.class)
                .withIdentity("reportGenerationJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger reportGenerationTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(reportGenerationJobDetail())
                .withIdentity("reportGenerationTrigger")
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(1, 6, 0)) // Run monthly on 1st at 6 AM
                .build();
    }
}