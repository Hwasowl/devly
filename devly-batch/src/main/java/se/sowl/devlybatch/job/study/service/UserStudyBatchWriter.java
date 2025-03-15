package se.sowl.devlybatch.job.study.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class UserStudyBatchWriter implements ItemWriter<List<UserStudy>> {
    private final UserStudyRepository userStudyRepository;

    @Override
    public void write(Chunk<? extends List<UserStudy>> chunk) {
        List<UserStudy> allNewStudies = flattenChunk(chunk);

        if (!allNewStudies.isEmpty()) {
            saveAndLogNewStudies(allNewStudies);
        }
    }

    private List<UserStudy> flattenChunk(Chunk<? extends List<UserStudy>> chunk) {
        List<UserStudy> allNewStudies = new ArrayList<>();

        for (List<UserStudy> page : chunk) {
            allNewStudies.addAll(page);
        }

        return allNewStudies;
    }

    private void saveAndLogNewStudies(List<UserStudy> studies) {
        userStudyRepository.saveAll(studies);
        log.info("Saved {} new study assignments", studies.size());
    }
}
