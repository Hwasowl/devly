package se.sowl.devlyapi.word.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.dto.WordResponse;
import se.sowl.devlyapi.word.service.WordService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WordController.class)
@AutoConfigureRestDocs
@WithMockUser
class WordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WordService wordService;

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
        when(wordService.getList(anyLong())).thenReturn(response);

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
}
