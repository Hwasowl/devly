package se.sowl.devlyapi.pr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import se.sowl.devlyapi.pr.dto.PrResponse;
import se.sowl.devlyapi.pr.dto.comments.PrCommentsResponse;
import se.sowl.devlyapi.pr.dto.files.PrChangedFilesResponse;
import se.sowl.devlyapi.pr.dto.review.PrCommentReviewRequest;
import se.sowl.devlyapi.pr.dto.review.PrCommentReviewResponse;
import se.sowl.devlyapi.pr.service.PrChangedFilesService;
import se.sowl.devlyapi.pr.service.PrCommentService;
import se.sowl.devlyapi.pr.service.PrReviewService;
import se.sowl.devlyapi.pr.service.PrService;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;
import se.sowl.devlydomain.user.domain.User;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
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
class PrControllerTest {
    @MockBean
    private PrService prService;

    @MockBean
    private PrChangedFilesService prChangedFilesService;

    @MockBean
    private PrCommentService prCommentService;

    @MockBean
    private PrReviewService prReviewService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("학습 ID로 PR 정보를 조회한다")
    void getPrTest() throws Exception {
        // given
        List<String> labels = List.of("backend", "feature", "bug-fix");
        PrResponse response = new PrResponse(1L, "테스트 PR 제목", "테스트 PR 설명", labels);
        when(prService.getPrResponse(anyLong(), anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/pr/study/{studyId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("pr-info",
                pathParameters(
                    parameterWithName("studyId").description("학습 ID")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("result.id").description("PR ID"),
                    fieldWithPath("result.title").description("PR 제목"),
                    fieldWithPath("result.description").description("PR 설명"),
                    fieldWithPath("result.labels").description("PR 라벨 목록")
                )
            ));
    }

    @Test
    @DisplayName("PR ID로 변경 파일 정보를 조회한다")
    void getChangedFilesTest() throws Exception {
        // given

        List<PrChangedFile> changedFiles = List.of(
            new PrChangedFile("src/main/java/com/example/SingletonService.java", "Java", "public class SingletonService {\n\n    private static volatile SingletonService instance;\n\n    private SingletonService() {\n        // private constructor\n    }\n\n    public static SingletonService getInstance() {\n        if (instance == null) {\n            synchronized (SingletonService.class) {\n                if (instance == null) {\n                    instance = new SingletonService();\n                }\n            }\n        }\n        return instance;\n    }\n}", mock(Pr.class)),
            new PrChangedFile("src/test/java/com/example/SingletonServiceTest.java", "Java", "import org.junit.jupiter.api.Test;\nimport static org.junit.jupiter.api.Assertions.*;\n\npublic class SingletonServiceTest {\n\n    @Test\n    void testSingletonInstance() {\n        SingletonService instance1 = SingletonService.getInstance();\n        SingletonService instance2 = SingletonService.getInstance();\n        assertSame(instance1, instance2);\n    }\n}", mock(Pr.class))
        );
        PrChangedFilesResponse response = PrChangedFilesResponse.from(changedFiles);
        when(prChangedFilesService.getChangedFilesResponse(anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/pr/{prId}/changed-files", 1L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("pr-changed-files",
                pathParameters(
                    parameterWithName("prId").description("PR ID")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("result.files").description("변경 파일 목록"),
                    fieldWithPath("result.files[].id").description("파일 ID"),
                    fieldWithPath("result.files[].prId").description("PR ID"),
                    fieldWithPath("result.files[].fileName").description("파일 이름"),
                    fieldWithPath("result.files[].language").description("프로그래밍 언어"),
                    fieldWithPath("result.files[].content").description("파일 내용")
                )
            ));
    }

    @Test
    @DisplayName("PR ID로 질문 정보를 조회한다")
    void getCommentsTest() throws Exception {
        // given
        List<PrComment> comments = List.of(
            new PrComment(0L, "이 부분은 어떻게 구현하면 좋을까요?", mock(Pr.class)),
            new PrComment(1L, "사용자가 슬라이더를 원하는 이미지로 넘길 수 있는 기능에 대한 의견을 여쭤보고 싶습니다.", mock(Pr.class))
        );
        PrCommentsResponse response = PrCommentsResponse.from(comments);
        when(prCommentService.getCommentsResponse(anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/pr/{prId}/comments", 1L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("pr-comments",
                pathParameters(
                    parameterWithName("prId").description("PR ID")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("result.comments").description("질문 목록"),
                    fieldWithPath("result.comments[].id").description("ID"),
                    fieldWithPath("result.comments[].sequence").description("인덱스"),
                    fieldWithPath("result.comments[].prId").description("PR ID"),
                    fieldWithPath("result.comments[].content").description("질문 내역")
                )
            ));
    }

    @Test
    @DisplayName("PR 커멘트에 대한 리뷰를 받는다.")
    void reviewPRComment() throws Exception {
        // given
        Long prCommentId = 1L;
        String requestAnswer = "이 커밋은 개인적으로 타당하지 못합니다. 그 이유는 ---";
        Long studyId = 1L;
        String responseReview = "동의합니다. 이 부분은 이런 부분이 타당하지 못하지만 이 부분은 다시 볼 필요가 ---";

        PrCommentReviewRequest request = new PrCommentReviewRequest(requestAnswer, studyId);
        PrCommentReviewResponse response = new PrCommentReviewResponse(responseReview);

        when(prReviewService.reviewPrComment(any(), any(), any(), any()))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/pr/review/comment/{prCommentId}", prCommentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andDo(document("pr-comment-review",
                pathParameters(
                    parameterWithName("prCommentId").description("PR 댓글 ID")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("result.review").description("피드백 내용")
                )
            ));
    }

    @Test
    @DisplayName("PR를 완료한다.")
    void completePrTest() throws Exception {
        // given
        Long prId = 1L;
        Long studyId = 2L;

        doNothing().when(prService).complete(any(), any(), any());

        // when & then
        mockMvc.perform(post("/api/pr/{prId}/study/{studyId}/done", prId, studyId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("pr-complete",
                pathParameters(
                    parameterWithName("prId").description("PR ID"),
                    parameterWithName("studyId").description("학습 ID")
                )
            ));
    }
}
