package se.sowl.devlyapi.pr.dto.files;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.sowl.devlydomain.pr.domain.PrChangedFile;

@Getter
@AllArgsConstructor
public class PrChangedFileResponse {
    private Long id;
    private Long prId;
    private String fileName;
    private String language;
    private String content;

    public static PrChangedFileResponse from(PrChangedFile prChangedFile) {
        return new PrChangedFileResponse(prChangedFile.getId(), prChangedFile.getPr().getId(), prChangedFile.getFileName(), prChangedFile.getLanguage(), prChangedFile.getContent());
    }
}
