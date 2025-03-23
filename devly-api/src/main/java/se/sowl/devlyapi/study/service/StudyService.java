package se.sowl.devlyapi.study.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.study.exception.DuplicateInitialUserStudiesException;
import se.sowl.devlyapi.study.exception.StudyNotExistException;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {
    private final UserStudyRepository userStudyRepository;
    private final StudyTypeRepository studyTypeRepository;
    private final StudyRepository studyRepository;

    @Transactional
    public void initialUserStudies(User user) {
        isStudyInitialed(user);
        try {
            List<StudyType> studyTypes = studyTypeRepository.findAll();
            for (StudyTypeEnum typeEnum : StudyTypeEnum.values()) {
                StudyType studyType = validateStudyType(typeEnum, studyTypes);
                Study study = studyRepository.findFirstByTypeId(studyType.getId())
                    .orElseThrow(() -> new StudyNotExistException("Study Not Exist"));
                UserStudy userStudy = UserStudy.builder().userId(user.getId()).study(study).build();
                userStudyRepository.save(userStudy);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize user studies", e);
        }
    }

    public Study getStudyById(Long id) {
        return studyRepository.findById(id).orElseThrow(
            () -> new StudyNotExistException("Study Not Exist")
        );
    }

    private void isStudyInitialed(User user) {
        if (userStudyRepository.existsByUserId(user.getId())) {
            throw new DuplicateInitialUserStudiesException("User already has studies");
        }
    }

    private static StudyType validateStudyType(StudyTypeEnum typeEnum, List<StudyType> studyTypes) {
        return studyTypes.stream()
            .filter(st -> st.getName().equals(typeEnum.getValue()))
            .findFirst()
            .orElseThrow(() -> new StudyNotExistException("Study type not found: " + typeEnum.getValue()));
    }
}

