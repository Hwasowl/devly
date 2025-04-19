package se.sowl.devlyapi.word.controller;

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
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.dto.WordResponse;
import se.sowl.devlyapi.word.dto.reviews.WordReviewResponse;
import se.sowl.devlyapi.word.service.WordReviewService;
import se.sowl.devlyapi.word.service.WordService;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;
import se.sowl.devlydomain.user.domain.User;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
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
class WordControllerTest {
    @MockBean
    private WordService wordService;

    @MockBean
    private WordReviewService wordReviewService;

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
    @DisplayName("학습 ID로 해당 학습의 단어 목록을 조회한다")
    void getWordListTest() throws Exception {
        // given
        List<WordResponse> words = List.of(
            new WordResponse(1L, "implementation", "구현, 실행",
                "{\"source\":\"React Documentation\",\"text\":\"The implementation details of React components should be hidden from their consumers.\",\"translation\":\"React 컴포넌트의 구현 세부사항은 해당 컴포넌트를 사용하는 쪽으로부터 숨겨져야 합니다.\"}",
                "{\"text\":\"\",\"distractors\":[\"Imitation\",\"Implication\",\"Realization\",\"Deployment\"]}",
                "/ˌɪmplɪmenˈteɪʃən/"),
            new WordResponse(2L, "polymorphism", "다형성",
                "{\"source\":\"Java Documentation\",\"text\":\"Polymorphism allows you to define one interface and have multiple implementations.\",\"translation\":\"다형성을 통해 하나의 인터페이스를 정의하고 여러 구현을 가질 수 있습니다.\"}",
                "{\"text\":\"\",\"distractors\":[\"Inheritance\",\"Encapsulation\",\"Abstraction\",\"Interface\"]}",
                "/ˌpɒlɪˈmɔːfɪzəm/")
        );
        WordListOfStudyResponse response = new WordListOfStudyResponse(words);
        when(wordService.getListResponse(anyLong(), anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/words/{studyId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("word-list",
                pathParameters(
                    parameterWithName("studyId").description("학습 ID")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("result.words").description("단어 목록"),
                    fieldWithPath("result.words[].id").description("단어 ID"),
                    fieldWithPath("result.words[].word").description("단어"),
                    fieldWithPath("result.words[].pronunciation").description("발음"),
                    fieldWithPath("result.words[].meaning").description("의미"),
                    fieldWithPath("result.words[].example").description("예문 정보 (JSON)"),
                    fieldWithPath("result.words[].quiz").description("퀴즈 정보 (JSON)")
                )
            ));
    }

    @Test
    @DisplayName("단어 학습에 대한 오답 노트를 제출한다")
    void createReviewTest() throws Exception {
        // given
        String request = "{\"correctIds\":[1,2],\"incorrectIds\":[3,4,5]}";
        WordReviewResponse response = new WordReviewResponse(List.of(1L, 2L), List.of(3L, 4L, 5L));
        when(wordService.getWordReviewsResponse(anyLong(), anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/words/review/study/{studyId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk())
            .andDo(document("create-word-review",
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

    @Test
    @DisplayName("단어 학습에 대한 오답 노트를 갱신한다.")
    void updateReviewTest() throws Exception {
        // given
        String request = "{\"correctIds\":[1,2]}";

        // when & then
        mockMvc.perform(put("/api/words/review/study/{studyId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk())
            .andDo(document("update-word-review",
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

    @Test
    @DisplayName("단어 학습에 대한 오답 정보를 조회한다")
    void getWordReviewsTest() throws Exception {
        // given
        WordReviewResponse response = new WordReviewResponse(List.of(1L, 2L, 3L), List.of(4L, 5L));
        when(wordService.getWordReviewsResponse(anyLong(), anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/words/review/study/{studyId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("get-word-reviews",
                pathParameters(
                    parameterWithName("studyId").description("학습 ID")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("result.correctIds").description("정답 ID 목록"),
                    fieldWithPath("result.incorrectIds").description("오답 ID 목록")
                )
            ));
    }
}
