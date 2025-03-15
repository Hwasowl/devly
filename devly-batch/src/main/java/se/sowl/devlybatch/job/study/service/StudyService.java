package se.sowl.devlybatch.job.study.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlybatch.job.study.cache.StudyCache;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.developer.repository.DeveloperTypeRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatusEnum;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StudyService {
    private final StudyRepository studyRepository;
    private final StudyTypeRepository studyTypeRepository;
    private final DeveloperTypeRepository developerTypeRepository;
    private final StudyCache studyCache;

    public List<Study> getTodayStudiesOf(Long StudyTypeId, StudyStatusEnum status) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return studyRepository.findByCreatedAtBetweenAndTypeIdAndStatus(startOfDay, endOfDay, StudyTypeId, status);
    }

    @Transactional
    public List<Study> generateStudiesOf() {
        // TODO: refactor this method when we have more study entity types
        List<StudyType> studyTypes = studyTypeRepository.findAll();
        StudyType wordType = findStudyTypeByName(studyTypes, "word");
        StudyType prType = findStudyTypeByName(studyTypes, "pr");

        List<DeveloperType> devTypes = developerTypeRepository.findAll();
        List<Study> studies = createStudiesForAllDevTypes(wordType, prType, devTypes);

        List<Study> savedStudies = studyRepository.saveAll(studies);
        studyCache.cacheStudies(savedStudies);

        return savedStudies;
    }

    private StudyType findStudyTypeByName(List<StudyType> studyTypes, String name) {
        return studyTypes.stream()
            .filter(studyType -> studyType.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("StudyType not found: " + name));
    }

    private List<Study> createStudiesForAllDevTypes(StudyType wordType, StudyType prType, List<DeveloperType> devTypes) {
        return devTypes.stream()
            .flatMap(devType -> Stream.of(
                createStudy(wordType.getId(), devType.getId()),
                createStudy(prType.getId(), devType.getId())
            ))
            .collect(Collectors.toList());
    }

    private Study createStudy(Long typeId, Long developerTypeId) {
        return Study.builder()
            .typeId(typeId)
            .developerTypeId(developerTypeId)
            .build();
    }
}
