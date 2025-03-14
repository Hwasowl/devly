package se.sowl.devlybatch.job.study.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.developer.repository.DeveloperTypeRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatusEnum;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyService {
    private final StudyRepository studyRepository;
    private final StudyTypeRepository studyTypeRepository;
    private final DeveloperTypeRepository developerTypeRepository;

    public List<Study> getTodayStudiesOf(Long StudyTypeId, StudyStatusEnum status) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return studyRepository.findByCreatedAtBetweenAndTypeIdAndStatus(startOfDay, endOfDay, StudyTypeId, status);
    }

    @Transactional
    public List<Study> generateStudiesOf() {
        // TODO: 스터디 배치 잡이 모두 구현 완료된다면 수정해야 한다. 현재 일부만 구현되었으므로 하드코딩으로 구현
        List<StudyType> studyTypes = studyTypeRepository.findAll();
        StudyType wordType = studyTypes.stream().filter(studyType -> studyType.getName().equals("word")).findFirst().get();
        StudyType prType = studyTypes.stream().filter(studyType -> studyType.getName().equals("pr")).findFirst().get();

        List<DeveloperType> devTypes = developerTypeRepository.findAll();
        List<Study> studies = new ArrayList<>(List.of());
        for(DeveloperType devType : devTypes) {
            Study study = Study.builder().typeId(wordType.getId()).developerTypeId(devType.getId()).build();
            Study prStudy = Study.builder().typeId(prType.getId()).developerTypeId(devType.getId()).build();
            studies.add(study);
            studies.add(prStudy);
        }
        return studyRepository.saveAll(studies);
    }
}
