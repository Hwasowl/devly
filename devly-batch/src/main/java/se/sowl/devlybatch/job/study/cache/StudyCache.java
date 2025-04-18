package se.sowl.devlybatch.job.study.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.sowl.devlydomain.study.domain.Study;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StudyCache {
    private final Map<String, List<Study>> studyCache = new ConcurrentHashMap<>();

    public void cacheStudies(List<Study> studies) {
        Map<String, List<Study>> groupedStudies = studies.stream()
            .collect(Collectors.groupingBy(this::createCacheKey));

        studyCache.putAll(groupedStudies);
        log.info("Cached {} study groups", groupedStudies.size());
    }

    public List<Study> getStudies(Long typeId, Long developerTypeId) {
        String cacheKey = createCacheKey(typeId, developerTypeId);
        return studyCache.getOrDefault(cacheKey, Collections.emptyList());
    }

    public void clearCache() {
        studyCache.clear();
        log.info("Study cache cleared");
    }

    public boolean isEmpty() {
        return studyCache.isEmpty();
    }

    private String createCacheKey(Study study) {
        return createCacheKey(study.getStudyType().getId(), study.getDeveloperType().getId());
    }

    private String createCacheKey(Long typeId, Long developerTypeId) {
        return typeId + ":" + developerTypeId;
    }
}
