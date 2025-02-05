package se.sowl.devlyapi.study.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WordReviewResponse {
    List<Long> correctIds;
    List<Long> incorrectIds;
}
