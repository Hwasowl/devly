package se.sowl.devlyapi.study.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateWordReviewRequest {
    List<Long> correctIds;
}
