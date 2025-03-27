package se.sowl.devlyapi.pr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.pr.dto.review.PrCommentReviewResponse;
import se.sowl.devlyapi.pr.exception.AlreadyPrReviewedException;
import se.sowl.devlyapi.study.service.StudyService;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.domain.PrReview;
import se.sowl.devlydomain.pr.repository.PrReviewRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.GptPromptManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrReviewService {
    private final GPTClient gptClient;
    private final GptPromptManager gptPromptManager;
    private final PrReviewEntityParser prReviewEntityParser;
    private final StudyService studyService;
    private final PrCommentService prCommentService;
    private final PrChangedFilesService prChangedFilesService;
    private final PrReviewRepository prReviewRepository;

    @Transactional
    public PrCommentReviewResponse reviewPrComment(Long userId, Long prCommentId, Long studyId, String answer) {
        validateAnswer(answer);
        validateIsAlreadyReviewed(prCommentId);
        Study study = studyService.getStudyById(studyId);
        GPTResponse gptResponse = gptClient.generate(prReviewEntityParser.createGPTRequest(
            createCommentReviewRequestPrompt(study.getDeveloperTypeId(), study.getTypeId(), prCommentId, answer)
        ));
        PrReview review = savePrReview(userId, prCommentId, answer, gptResponse);
        return new PrCommentReviewResponse(review.getReview());
    }

    private String createCommentReviewRequestPrompt (Long developerTypeId, Long studyTypeId, Long prCommentId, String answer) {
        StringBuilder prompt = new StringBuilder();
        gptPromptManager.addRolePrompt(developerTypeId, studyTypeId, prompt);
        addPrCommentPrompt(prCommentId, prompt);
        addUserAnswerPrompt(answer, prompt);
        addCodePrompt(prCommentId, prompt);
        return prompt.toString();
    }

    private void addPrCommentPrompt(Long prCommentId, StringBuilder prompt) {
        PrComment prComment = prCommentService.getCommentById(prCommentId);
        prompt.append("질문 내용: ").append(prComment.getContent());
    }

    private void addUserAnswerPrompt(String answer, StringBuilder prompt) {
        prompt.append("유저 답변: ").append(answer);
    }

    private void addCodePrompt(Long prCommentId, StringBuilder prompt) {
        PrComment comment = prCommentService.getCommentById(prCommentId);
        List<PrChangedFile> changedFiles = prChangedFilesService.getChangedFileById(comment.getPrId());
        changedFiles.stream()
            .map(PrChangedFile::getContent)
            .forEach(code -> prompt.append("코드: ").append(code));
    }

    private PrReview savePrReview(Long userId, Long prCommentId, String answer, GPTResponse gptResponse) {
        PrReview review = prReviewEntityParser.parseEntity(createParameters(userId, prCommentId, answer), gptResponse.getContent());
        return prReviewRepository.save(review);
    }

    private ParserArguments createParameters(Long userId, Long prCommentId, String answer) {
        return new ParserArguments()
            .add("userId", userId)
            .add("prCommentId", prCommentId)
            .add("answer", answer);
    }

    private void validateAnswer(String answer) {
        if (answer.isBlank() || answer.isEmpty()) {
            throw new IllegalArgumentException("Cannot review empty answer comment. Please check your argument");
        }
    }

    private void validateIsAlreadyReviewed(Long prCommentId) {
        if (prReviewRepository.findByPrCommentId(prCommentId).isPresent()) {
            throw new AlreadyPrReviewedException("Already reviewed comment. Please check your argument");
        }
    }
}
