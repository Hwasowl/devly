package se.sowl.devlyapi.word.dto.reviews;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateWordReviewRequest {
    List<Long> correctIds;
    List<Long> incorrectIds;
}
