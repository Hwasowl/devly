package se.sowl.devlyapi.study.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
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
import se.sowl.devlyapi.study.dto.UserStudyTask;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlyapi.study.service.StudyService;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;
import se.sowl.devlydomain.user.domain.User;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@WithMockUser
@ActiveProfiles("test")
class StudyControllerTest {
    @MockBean
    private StudyService studyService;

    @Autowired
    private MockMvc mockMvc;

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
    }

    @Test
    @DisplayName("사용자의 학습 진행 상태를 조회한다")
    void getUserStudyTasksTest() throws Exception {
        // given
        UserStudyTask wordTask = new UserStudyTask(1L, 10L, false);
        UserStudyTask knowledgeTask = new UserStudyTask(2L, 0L, true);
        UserStudyTask prTask = new UserStudyTask(3L, 0L, true);
        UserStudyTask discussionTask = new UserStudyTask(4L, 0L, true);

        UserStudyTasksResponse response = new UserStudyTasksResponse(
            wordTask, knowledgeTask, prTask, discussionTask
        );

        when(studyService.getUserStudyTasks(anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/studies/tasks")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("study-tasks",
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("result.word").description("단어 학습 정보"),
                    fieldWithPath("result.word.studyId").description("단어 학습 ID"),
                    fieldWithPath("result.word.total").description("총 단어 수"),
                    fieldWithPath("result.word.completed").description("완료 여부"),
                    fieldWithPath("result.knowledge").description("지식 학습 정보"),
                    fieldWithPath("result.knowledge.studyId").description("지식 학습 ID"),
                    fieldWithPath("result.knowledge.total").description("총 지식 수"),
                    fieldWithPath("result.knowledge.completed").description("완료 여부"),
                    fieldWithPath("result.pr").description("PR 학습 정보"),
                    fieldWithPath("result.pr.studyId").description("PR 학습 ID"),
                    fieldWithPath("result.pr.total").description("총 PR 수"),
                    fieldWithPath("result.pr.completed").description("완료 여부"),
                    fieldWithPath("result.discussion").description("토론 학습 정보"),
                    fieldWithPath("result.discussion.studyId").description("토론 학습 ID"),
                    fieldWithPath("result.discussion.total").description("총 토론 수"),
                    fieldWithPath("result.discussion.completed").description("완료 여부")
                )
            ));
    }

    @Test
    @DisplayName("단어 학습에 대한 오답 정보를 제출한다")
    void reviewTest() throws Exception {
        // given
        String request = "{\"correctIds\":[1,2],\"incorrectIds\":[3,4,5]}";

        // when & then
        mockMvc.perform(post("/api/studies/{studyId}/words/review", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk())
            .andDo(document("word-review",
                pathParameters(
                    parameterWithName("studyId").description("학습 ID")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("result").description("결과")
                )
            ));
    }
}
