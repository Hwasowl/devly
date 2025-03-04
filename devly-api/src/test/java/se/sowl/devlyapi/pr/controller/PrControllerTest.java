package se.sowl.devlyapi.pr.controller;

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
import se.sowl.devlyapi.pr.service.PrService;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;
import se.sowl.devlydomain.user.domain.User;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
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
    @DisplayName("학습 ID로 PR 정보를 조회한다")
    void getPrTest() throws Exception {
        // given
        List<String> labels = List.of("backend", "feature", "bug-fix");
        PrResponse response = new PrResponse(1L, "테스트 PR 제목", "테스트 PR 설명", labels);
        when(prService.getPr(anyLong(), anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/pr/{studyId}", 1L)
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
}
