package se.sowl.devlybatch.job.study.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StudyAssignmentListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Study assignment step started");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Study assignment step completed. Read count: {}", stepExecution.getReadCount());
        return ExitStatus.COMPLETED;
    }
}

