package se.sowl.devlyapi.pr.dto.files;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.sowl.devlydomain.pr.domain.PrChangedFile;

import java.util.List;

@Getter
@AllArgsConstructor
public class PrChangedFilesResponse {
    private List<PrChangedFileResponse> files;

    public static PrChangedFilesResponse from(List<PrChangedFile> files) {
        return new PrChangedFilesResponse(files.stream().map(PrChangedFileResponse::from).toList());
    }
}
