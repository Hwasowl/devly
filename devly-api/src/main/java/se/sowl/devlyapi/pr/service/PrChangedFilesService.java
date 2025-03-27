package se.sowl.devlyapi.pr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.pr.dto.files.PrChangedFilesResponse;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.repository.PrChangedFileRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrChangedFilesService {
    private final PrChangedFileRepository prChangedFileRepository;

    public PrChangedFilesResponse getChangedFilesResponse(Long prId) {
        List<PrChangedFile> files = prChangedFileRepository.findByPrId(prId);
        return PrChangedFilesResponse.from(files);
    }

    public List<PrChangedFile> getChangedFileById(Long id) {
        return prChangedFileRepository.findByPrId(id);
    }
}
