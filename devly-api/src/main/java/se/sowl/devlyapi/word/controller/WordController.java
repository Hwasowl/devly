package se.sowl.devlyapi.word.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.devlyapi.common.CommonResponse;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.service.WordService;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping("/{studyId}")
    public CommonResponse<WordListOfStudyResponse> getWords(@PathVariable Long studyId) {
        WordListOfStudyResponse response = wordService.getList(studyId);
        return CommonResponse.ok(response);
    }
}
