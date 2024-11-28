package se.sowl.devlyapi.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ServletFilterExceptionHandler extends OncePerRequestFilter implements OrderedFilter {

    private final ObjectMapper objectMapper;

    public ServletFilterExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handleFilterException(response, e);
        }
    }

    private void handleFilterException(HttpServletResponse response, Exception e) throws IOException {
        CommonResponse<Void> errorResponse;
        int status;

        if (e instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST.value();
            errorResponse = CommonResponse.fail(e.getMessage());
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR.value();
            errorResponse = CommonResponse.fail("서버에 문제가 생겼어요. 잠시 후 다시 시도해주세요.");
        }

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
