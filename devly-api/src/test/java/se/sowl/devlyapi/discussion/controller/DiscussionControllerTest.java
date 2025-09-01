package se.sowl.devlyapi.discussion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import se.sowl.devlyapi.discussion.controller.dto.DiscussionStartRequest;
import se.sowl.devlyapi.discussion.service.DiscussionService;
import se.sowl.devlyapi.discussion.service.dto.AnswerRequest;
import se.sowl.devlyapi.discussion.service.dto.DiscussionResultResponse;
import se.sowl.devlyapi.discussion.service.dto.DiscussionStartResponse;
import se.sowl.devlyapi.discussion.service.dto.RoundResponse;
import se.sowl.devlydomain.discussion.domain.DiscussionTopic;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;
import se.sowl.devlydomain.user.domain.User;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@ActiveProfiles("test")
class DiscussionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DiscussionService discussionService;

    private DiscussionTopic testTopic;
    private DiscussionStartResponse testStartResponse;
    private RoundResponse testRoundResponse;
    private DiscussionResultResponse testResultResponse;

    @BeforeEach
    void setUp() {
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(
            mock(User.class),
            mock(OAuth2User.class)
        );
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
            customOAuth2User,
            customOAuth2User.getAuthorities(),
            "google"
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        testTopic = DiscussionTopic.builder()
            .title("Spring Framework")
            .description("Spring 기초 면접")
            .category("BACKEND")
            .difficulty("INTERMEDIATE")
            .initialQuestion("Spring의 IoC에 대해 설명해주세요.")
            .build();

        testStartResponse = DiscussionStartResponse.builder()
            .discussionId(1L)
            .topic("Spring Framework")
            .description("Spring 기초 면접")
            .firstQuestion("Spring의 IoC에 대해 설명해주세요.")
            .currentRound(1)
            .totalRounds(3)
            .build();

        testRoundResponse = RoundResponse.builder()
            .feedback("좋은 설명입니다.")
            .score(8.5)
            .nextQuestion("DI와 IoC의 차이점은 무엇인가요?")
            .currentRound(2)
            .isCompleted(false)
            .build();

        testResultResponse = DiscussionResultResponse.builder()
            .discussionId(1L)
            .topic("Spring Framework")
            .rounds(Arrays.asList())
            .overallScore(8.2)
            .finalFeedback("전반적으로 우수한 답변이었습니다.")
            .build();
    }

    @Test
    @DisplayName("면접 주제 목록을 조회할 수 있어야 한다")
    void getDiscussionTopics() throws Exception {
        // given
        List<DiscussionTopic> topics = Arrays.asList(testTopic);
        when(discussionService.getDiscussionTopics()).thenReturn(topics);

        // when & then
        mockMvc.perform(get("/api/discussions/topics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("Spring Framework"))
            .andExpect(jsonPath("$[0].category").value("BACKEND"))
            .andDo(print());

        verify(discussionService).getDiscussionTopics();
    }

    @Test
    @DisplayName("카테고리별 면접 주제를 조회할 수 있어야 한다")
    void getDiscussionTopicsByCategory() throws Exception {
        // given
        String category = "BACKEND";
        List<DiscussionTopic> topics = Arrays.asList(testTopic);
        when(discussionService.getDiscussionTopicsByCategory(category)).thenReturn(topics);

        // when & then
        mockMvc.perform(get("/api/discussions/topics")
                .param("category", category))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].category").value("BACKEND"))
            .andDo(print());

        verify(discussionService).getDiscussionTopicsByCategory(category);
    }

    @Test
    @DisplayName("면접을 시작할 수 있어야 한다")
    void startDiscussion() throws Exception {
        // given
        DiscussionStartRequest request = new DiscussionStartRequest(1L, 1L);
        when(discussionService.startDiscussion(1L, 1L)).thenReturn(testStartResponse);

        // when & then
        mockMvc.perform(post("/api/discussions/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.discussionId").value(1L))
            .andExpect(jsonPath("$.topic").value("Spring Framework"))
            .andExpect(jsonPath("$.firstQuestion").value("Spring의 IoC에 대해 설명해주세요."))
            .andExpect(jsonPath("$.currentRound").value(1))
            .andExpect(jsonPath("$.totalRounds").value(3))
            .andDo(print());

        verify(discussionService).startDiscussion(1L, 1L);
    }

    @Test
    @DisplayName("답변을 제출할 수 있어야 한다")
    void submitAnswer() throws Exception {
        // given
        Long discussionId = 1L;
        AnswerRequest request = new AnswerRequest(1, "IoC는 제어의 역전을 의미합니다.");
        when(discussionService.submitAnswer(eq(discussionId), any(AnswerRequest.class)))
            .thenReturn(testRoundResponse);

        // when & then
        mockMvc.perform(post("/api/discussions/{discussionId}/answer", discussionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.feedback").value("좋은 설명입니다."))
            .andExpect(jsonPath("$.score").value(8.5))
            .andExpect(jsonPath("$.nextQuestion").value("DI와 IoC의 차이점은 무엇인가요?"))
            .andExpect(jsonPath("$.currentRound").value(2))
            .andExpect(jsonPath("$.isCompleted").value(false))
            .andDo(print());

        verify(discussionService).submitAnswer(eq(discussionId), any(AnswerRequest.class));
    }

    @Test
    @DisplayName("면접 결과를 조회할 수 있어야 한다")
    void getDiscussionResult() throws Exception {
        // given
        Long discussionId = 1L;
        when(discussionService.getDiscussionResult(discussionId)).thenReturn(testResultResponse);

        // when & then
        mockMvc.perform(get("/api/discussions/{discussionId}/result", discussionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.discussionId").value(1L))
            .andExpect(jsonPath("$.topic").value("Spring Framework"))
            .andExpect(jsonPath("$.overallScore").value(8.2))
            .andExpect(jsonPath("$.finalFeedback").value("전반적으로 우수한 답변이었습니다."))
            .andExpect(jsonPath("$.rounds").isArray())
            .andDo(print());

        verify(discussionService).getDiscussionResult(discussionId);
    }

    @Test
    @DisplayName("잘못된 요청 시 400 에러가 발생해야 한다")
    void invalidRequestShouldReturn400() throws Exception {
        // given
        DiscussionStartRequest invalidRequest = new DiscussionStartRequest(null, null);

        // when & then
        mockMvc.perform(post("/api/discussions/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 면접 결과 조회 시 예외가 발생해야 한다")
    void getNonExistentDiscussionResultShouldThrowException() throws Exception {
        // given
        Long discussionId = 999L;
        when(discussionService.getDiscussionResult(discussionId))
            .thenThrow(new IllegalArgumentException("면접을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api/discussions/{discussionId}/result", discussionId))
            .andExpect(status().isBadRequest())
            .andDo(print());

        verify(discussionService).getDiscussionResult(discussionId);
    }
}