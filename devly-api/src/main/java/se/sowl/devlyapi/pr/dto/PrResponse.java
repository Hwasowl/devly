package se.sowl.devlyapi.pr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrLabel;

import java.util.List;

@Getter
@AllArgsConstructor
public class PrResponse {
    private Long id;
    private String title;
    private String description;
    private List<String> labels;

    public static PrResponse from(Pr pr, List<PrLabel> prLabel) {
        return new PrResponse(pr.getId(), pr.getTitle(), pr.getDescription(), prLabel.stream().map(PrLabel::getLabel).toList());
    }
}
