package se.sowl.devlyapi.discussion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.discussion.service.dto.AnswerRequest;
import se.sowl.devlyapi.discussion.service.dto.DiscussionResultResponse;
import se.sowl.devlyapi.discussion.service.dto.DiscussionStartResponse;
import se.sowl.devlyapi.discussion.service.dto.RoundResponse;
import se.sowl.devlydomain.discussion.domain.Discussion;
import se.sowl.devlydomain.discussion.domain.DiscussionRound;
import se.sowl.devlydomain.discussion.domain.DiscussionStatus;
import se.sowl.devlydomain.discussion.domain.DiscussionTopic;
import se.sowl.devlydomain.discussion.repository.DiscussionRepository;
import se.sowl.devlydomain.discussion.repository.DiscussionRoundRepository;
import se.sowl.devlydomain.discussion.repository.DiscussionTopicRepository;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.repository.UserRepository;
import se.sowl.devlyexternal.client.gpt.dto.DiscussionGPTRequest;
import se.sowl.devlyexternal.client.gpt.dto.DiscussionGPTResponse;
import se.sowl.devlyexternal.client.gpt.service.DiscussionGPTService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscussionService {
    private final DiscussionRepository discussionRepository;
    private final DiscussionTopicRepository discussionTopicRepository;
    private final DiscussionRoundRepository discussionRoundRepository;
    private final UserRepository userRepository;
    private final DiscussionGPTService discussionGPTService;
    
    @Transactional
    public DiscussionStartResponse startDiscussion(Long userId, Long topicId) {
        User user = getUserById(userId);
        DiscussionTopic topic = getTopicById(topicId);

        discussionRepository.findLatestByUserIdAndStatus(userId, DiscussionStatus.IN_PROGRESS)
            .ifPresent(discussion -> {
                throw new IllegalStateException("이미 진행 중인 면접이 있습니다.");
            });
        
        Discussion discussion = Discussion.builder()
            .user(user)
            .topic(topic)
            .build();
        
        discussion.start();
        Discussion savedDiscussion = discussionRepository.save(discussion);

        DiscussionRound firstRound = DiscussionRound.builder()
            .discussion(savedDiscussion)
            .roundNumber(1)
            .question(topic.getInitialQuestion())
            .build();
        
        discussionRoundRepository.save(firstRound);
        
        return DiscussionStartResponse.builder()
            .discussionId(savedDiscussion.getId())
            .topic(topic.getTitle())
            .description(topic.getDescription())
            .firstQuestion(topic.getInitialQuestion())
            .currentRound(1)
            .totalRounds(3)
            .build();
    }
    
    @Transactional
    public RoundResponse submitAnswer(Long discussionId, AnswerRequest request) {
        Discussion discussion = getDiscussionById(discussionId);
        
        if (!discussion.isInProgress()) {
            throw new IllegalStateException("진행 중인 면접이 아닙니다.");
        }
        
        DiscussionRound currentRound = discussionRoundRepository
            .findByDiscussionIdAndRoundNumber(discussionId, request.getRoundNumber())
            .orElseThrow(() -> new IllegalArgumentException("해당 라운드를 찾을 수 없습니다."));

        currentRound.submitAnswer(request.getAnswer());
        discussionRoundRepository.save(currentRound);

        DiscussionGPTResponse gptResponse = requestGPTEvaluation(discussion, currentRound);

        currentRound.setFeedback(gptResponse.getFeedback(), gptResponse.getScore());
        discussionRoundRepository.save(currentRound);

        if (discussion.isLastRound()) {
            completeDiscussion(discussion, gptResponse);
            return RoundResponse.builder()
                .feedback(gptResponse.getFeedback())
                .score(gptResponse.getScore())
                .currentRound(discussion.getCurrentRound())
                .isCompleted(true)
                .build();
        } else {
            createNextRound(discussion, gptResponse.getNextQuestion());
            return RoundResponse.builder()
                .feedback(gptResponse.getFeedback())
                .score(gptResponse.getScore())
                .nextQuestion(gptResponse.getNextQuestion())
                .currentRound(discussion.getCurrentRound())
                .isCompleted(false)
                .build();
        }
    }
    
    public DiscussionResultResponse getDiscussionResult(Long discussionId) {
        Discussion discussion = getDiscussionById(discussionId);
        if (!discussion.isCompleted()) {
            throw new IllegalStateException("완료되지 않은 면접입니다.");
        }
        List<DiscussionRound> rounds = discussionRoundRepository.findByDiscussionIdOrderByRoundNumberAsc(discussionId);
        
        return DiscussionResultResponse.from(discussion, rounds);
    }
    
    public List<DiscussionTopic> getDiscussionTopics() {
        return discussionTopicRepository.findAll();
    }
    
    public List<DiscussionTopic> getDiscussionTopicsByCategory(String category) {
        return discussionTopicRepository.findByCategory(category);
    }

    @Transactional
    protected void createNextRound(Discussion discussion, String nextQuestion) {
        discussion.proceedToNextRound();
        discussionRepository.save(discussion);

        DiscussionRound nextRound = DiscussionRound.builder()
                .discussion(discussion)
                .roundNumber(discussion.getCurrentRound())
                .question(nextQuestion)
                .build();

        discussionRoundRepository.save(nextRound);
    }

    @Transactional
    protected void completeDiscussion(Discussion discussion, DiscussionGPTResponse gptResponse) {
        if (gptResponse.isFinalRound()) {
            DiscussionGPTResponse.FinalEvaluation finalEval = gptResponse.getFinalEvaluation();
            discussion.complete(finalEval.getSummary(), finalEval.getOverallScore());
        } else {
            discussion.complete("면접이 완료되었습니다.", gptResponse.getScore());
        }
        discussionRepository.save(discussion);
    }
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
    
    private DiscussionTopic getTopicById(Long topicId) {
        return discussionTopicRepository.findById(topicId)
            .orElseThrow(() -> new IllegalArgumentException("주제를 찾을 수 없습니다."));
    }
    
    private Discussion getDiscussionById(Long discussionId) {
        return discussionRepository.findById(discussionId)
            .orElseThrow(() -> new IllegalArgumentException("면접을 찾을 수 없습니다."));
    }
    
    private DiscussionGPTResponse requestGPTEvaluation(Discussion discussion, DiscussionRound currentRound) {
        DiscussionGPTRequest gptRequest = DiscussionGPTRequest.builder()
            .topic(discussion.getTopic().getTitle())
            .category(discussion.getTopic().getCategory())
            .difficulty(discussion.getTopic().getDifficulty())
            .currentRound(currentRound.getRoundNumber())
            .totalRounds(3)
            .currentQuestion(currentRound.getQuestion())
            .userAnswer(currentRound.getUserAnswer())
            .build();
        
        return discussionGPTService.evaluateAnswer(gptRequest);
    }
}