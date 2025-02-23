package se.sowl.devlybatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JobLauncherController {
    private final JobLauncher jobLauncher;
    private final Job dailyStudyJob;

    @PostMapping("/api/batch/daily-study")
    public ResponseEntity<String> launchDailyStudyJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
            jobLauncher.run(dailyStudyJob, jobParameters);
            return ResponseEntity.ok("Requested Daily study job started");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to start job: " + e.getMessage());
        }
    }
}
