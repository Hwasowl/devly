package se.sowl.devlyapi.pr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.sowl.devlydomain.pr.domain.PrReview;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.GptEntityParser;
import se.sowl.devlyexternal.common.gpt.GptRequestFactory;
import se.sowl.devlyexternal.common.gpt.GptResponseValidator;
import se.sowl.devlyexternal.common.gpt.exception.GPTContentProcessingException;

@Slf4j
@Component
public class PrReviewEntityParser extends GptEntityParser<PrReview> {

    public PrReviewEntityParser(GptRequestFactory requestFactory, GptResponseValidator responseValidator) {
        super(requestFactory, responseValidator);
    }

    @Override
    protected PrReview parseEntity(ParserArguments parameters, String entry) {
        Long userId = parameters.get("userId", Long.class);
        Long prCommentId = parameters.get("prCommentId", Long.class);
        String answer = parameters.get("answer", String.class);
        try {
            return PrReview.builder()
                .userId(userId)
                .prCommentId(prCommentId)
                .answer(answer)
                .review(entry)
                .build();
        } catch (Exception e) {
            throw new GPTContentProcessingException("Error parsing PrReview entity: " + e.getMessage(), e);
        }
    }
}
