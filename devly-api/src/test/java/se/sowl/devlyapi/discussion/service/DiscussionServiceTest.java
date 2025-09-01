package se.sowl.devlyapi.discussion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sowl.devlyapi.discussion.service.dto.AnswerRequest;
import se.sowl.devlyapi.discussion.service.dto.DiscussionResultResponse;
import se.sowl.devlyapi.discussion.service.dto.DiscussionStartResponse;
import se.sowl.devlyapi.discussion.service.dto.RoundResponse;
import se.sowl.devlydomain.discussion.domain.*;
import se.sowl.devlydomain.discussion.repository.DiscussionRepository;
import se.sowl.devlydomain.discussion.repository.DiscussionRoundRepository;
import se.sowl.devlydomain.discussion.repository.DiscussionTopicRepository;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.repository.UserRepository;
import se.sowl.devlyexternal.client.gpt.dto.DiscussionGPTResponse;
import se.sowl.devlyexternal.client.gpt.service.DiscussionGPTService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DiscussionServiceTest {

    @Mock
    private DiscussionRepository discussionRepository;
    
    @Mock
    private DiscussionTopicRepository discussionTopicRepository;
    
    @Mock
    private DiscussionRoundRepository discussionRoundRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private DiscussionGPTService discussionGPTService;
    
    @InjectMocks
    private DiscussionService discussionService;

    private User testUser;
    private DiscussionTopic testTopic;
    private Discussion testDiscussion;

    @BeforeEach
    void setUp() {
        testUser = User.builder().email("test@test.com").build();
        testTopic = DiscussionTopic.builder()
            .title("Spring Framework")
            .description("Spring 기초 면접")
            .category("BACKEND")
            .difficulty("INTERMEDIATE")
            .initialQuestion("Spring의 IoC에 대해 설명해주세요.")
            .build();
        testDiscussion = Discussion.builder()
            .user(testUser)
            .topic(testTopic)
            .build();
        ReflectionTestUtils.setField(testDiscussion, "id", 1L);
    }

    @Test
    @DisplayName("면접을 성공적으로 시작할 수 있어야 한다")
    void startDiscussionSuccessfully() {
        // given
        Long userId = 1L;
        Long topicId = 1L;
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(discussionTopicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(discussionRepository.findLatestByUserIdAndStatus(userId, DiscussionStatus.IN_PROGRESS))
            .thenReturn(Optional.empty());
        when(discussionRepository.save(any(Discussion.class))).thenReturn(testDiscussion);
        when(discussionRoundRepository.save(any(DiscussionRound.class))).thenReturn(mock(DiscussionRound.class));

        // when
        DiscussionStartResponse response = discussionService.startDiscussion(userId, topicId);

        // then
        assertNotNull(response);
        assertEquals(testTopic.getTitle(), response.getTopic());
        assertEquals(testTopic.getDescription(), response.getDescription());
        assertEquals(testTopic.getInitialQuestion(), response.getFirstQuestion());
        assertEquals(1, response.getCurrentRound());
        assertEquals(3, response.getTotalRounds());
        
        verify(discussionRepository).save(any(Discussion.class));
        verify(discussionRoundRepository).save(any(DiscussionRound.class));
    }

    @Test
    @DisplayName("이미 진행 중인 면접이 있으면 예외가 발생해야 한다")
    void startDiscussionWithExistingInProgressShouldThrowException() {
        // given
        Long userId = 1L;
        Long topicId = 1L;
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(discussionTopicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(discussionRepository.findLatestByUserIdAndStatus(userId, DiscussionStatus.IN_PROGRESS))
            .thenReturn(Optional.of(testDiscussion));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> discussionService.startDiscussion(userId, topicId));
        assertEquals("이미 진행 중인 면접이 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("답변을 제출하고 피드백을 받을 수 있어야 한다")
    void submitAnswerSuccessfully() {
        // given
        Long discussionId = 1L;
        AnswerRequest request = new AnswerRequest(1, "IoC는 제어의 역전을 의미합니다.");
        
        testDiscussion.start();
        DiscussionRound round = DiscussionRound.builder()
            .discussion(testDiscussion)
            .roundNumber(1)
            .question("Spring의 IoC에 대해 설명해주세요.")
            .build();
        
        DiscussionGPTResponse gptResponse = new DiscussionGPTResponse(
            "좋은 설명입니다.", 8.5, "DI와 IoC의 차이점은 무엇인가요?", null
        );

        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(testDiscussion));
        when(discussionRoundRepository.findByDiscussionIdAndRoundNumber(discussionId, 1))
            .thenReturn(Optional.of(round));
        when(discussionGPTService.evaluateAnswer(any())).thenReturn(gptResponse);
        when(discussionRoundRepository.save(any(DiscussionRound.class))).thenReturn(round);

        // when
        RoundResponse response = discussionService.submitAnswer(discussionId, request);

        // then
        assertNotNull(response);
        assertEquals("좋은 설명입니다.", response.getFeedback());
        assertEquals(8.5, response.getScore());
        assertEquals("DI와 IoC의 차이점은 무엇인가요?", response.getNextQuestion());
        assertEquals(2, response.getCurrentRound());
        assertEquals(false, response.getIsCompleted());
    }

    @Test
    @DisplayName("마지막 라운드에서는 면접을 완료해야 한다")
    void submitAnswerInLastRoundShouldCompleteDiscussion() {
        // given
        Long discussionId = 1L;
        AnswerRequest request = new AnswerRequest(3, "마지막 답변입니다.");
        
        testDiscussion.start();
        testDiscussion.proceedToNextRound();
        testDiscussion.proceedToNextRound(); // 3라운드
        
        DiscussionRound round = DiscussionRound.builder()
            .discussion(testDiscussion)
            .roundNumber(3)
            .question("마지막 질문입니다.")
            .build();
        
        DiscussionGPTResponse.FinalEvaluation finalEval = new DiscussionGPTResponse.FinalEvaluation(
            8.0, Arrays.asList("논리적 사고", "명확한 설명"), 
            Arrays.asList("구체적 예시 부족"), "전반적으로 우수한 답변이었습니다."
        );
        DiscussionGPTResponse gptResponse = new DiscussionGPTResponse(
            "훌륭한 마무리였습니다.", 8.0, null, finalEval
        );

        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(testDiscussion));
        when(discussionRoundRepository.findByDiscussionIdAndRoundNumber(discussionId, 3))
            .thenReturn(Optional.of(round));
        when(discussionGPTService.evaluateAnswer(any())).thenReturn(gptResponse);
        when(discussionRoundRepository.save(any(DiscussionRound.class))).thenReturn(round);

        // when
        RoundResponse response = discussionService.submitAnswer(discussionId, request);

        // then
        assertNotNull(response);
        assertEquals("훌륭한 마무리였습니다.", response.getFeedback());
        assertEquals(8.0, response.getScore());
        assertEquals(true, response.getIsCompleted());
        assertNull(response.getNextQuestion());
    }

    @Test
    @DisplayName("완료된 면접 결과를 조회할 수 있어야 한다")
    void getDiscussionResultSuccessfully() {
        // given
        Long discussionId = 1L;
        testDiscussion.start();
        testDiscussion.complete("전반적으로 우수했습니다.", 8.2);
        
        List<DiscussionRound> rounds = Arrays.asList(
            DiscussionRound.builder()
                .discussion(testDiscussion)
                .roundNumber(1)
                .question("첫 번째 질문")
                .build()
        );

        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(testDiscussion));
        when(discussionRoundRepository.findByDiscussionIdOrderByRoundNumberAsc(discussionId))
            .thenReturn(rounds);

        // when
        DiscussionResultResponse response = discussionService.getDiscussionResult(discussionId);

        // then
        assertNotNull(response);
        assertEquals(discussionId, response.getDiscussionId());
        assertEquals(testTopic.getTitle(), response.getTopic());
        assertEquals(8.2, response.getOverallScore());
        assertEquals("전반적으로 우수했습니다.", response.getFinalFeedback());
        assertEquals(1, response.getRounds().size());
    }

    @Test
    @DisplayName("완료되지 않은 면접 결과를 조회하면 예외가 발생해야 한다")
    void getDiscussionResultOfIncompleteDiscussionShouldThrowException() {
        // given
        Long discussionId = 1L;
        testDiscussion.start(); // 시작했지만 완료하지 않음

        when(discussionRepository.findById(discussionId)).thenReturn(Optional.of(testDiscussion));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> discussionService.getDiscussionResult(discussionId));
        assertEquals("완료되지 않은 면접입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("모든 면접 주제를 조회할 수 있어야 한다")
    void getDiscussionTopicsSuccessfully() {
        // given
        List<DiscussionTopic> topics = Arrays.asList(testTopic);
        when(discussionTopicRepository.findAll()).thenReturn(topics);

        // when
        List<DiscussionTopic> result = discussionService.getDiscussionTopics();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTopic, result.get(0));
    }

    @Test
    @DisplayName("카테고리별 면접 주제를 조회할 수 있어야 한다")
    void getDiscussionTopicsByCategorySuccessfully() {
        // given
        String category = "BACKEND";
        List<DiscussionTopic> topics = Arrays.asList(testTopic);
        when(discussionTopicRepository.findByCategory(category)).thenReturn(topics);

        // when
        List<DiscussionTopic> result = discussionService.getDiscussionTopicsByCategory(category);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTopic, result.get(0));
    }
}