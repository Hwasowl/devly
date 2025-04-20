package se.sowl.devlyapi.pr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.sowl.devlyapi.user.service.UserService;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.domain.PrReview;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.GptEntityParser;
import se.sowl.devlyexternal.common.gpt.GptRequestFactory;
import se.sowl.devlyexternal.common.gpt.GptResponseValidator;
import se.sowl.devlyexternal.common.gpt.exception.GPTContentProcessingException;

@Slf4j
@Component
public class PrReviewEntityParser extends GptEntityParser<PrReview> {

    private final UserService userService;
    private final PrCommentService prCommentService;

    public PrReviewEntityParser(
        GptRequestFactory requestFactory,
        GptResponseValidator responseValidator,
        UserService userService,
        PrCommentService prCommentService
    ) {
        super(requestFactory, responseValidator);
        this.userService = userService;
        this.prCommentService = prCommentService;
    }

    @Override
    protected PrReview parseEntity(ParserArguments parameters, String entry) {
        User user = userService.getUserById(parameters.get("userId", Long.class));
        PrComment comment = prCommentService.getCommentById(parameters.get("prCommentId", Long.class));
        String answer = parameters.get("answer", String.class);
        try {
            return PrReview.builder()
                .user(user)
                .comment(comment)
                .answer(answer)
                .review(entry)
                .build();
        } catch (Exception e) {
            throw new GPTContentProcessingException("Error parsing PrReview entity: " + e.getMessage(), e);
        }
    }
}
