package se.sowl.devlyapi.pr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.pr.dto.comments.PrCommentsResponse;
import se.sowl.devlyapi.pr.exception.PrCommentNotExistException;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.repository.PrCommentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrCommentService {
    private final PrCommentRepository prCommentRepository;

    public PrCommentsResponse getCommentsResponse(Long prId) {
        List<PrComment> comments = prCommentRepository.findByPrId(prId);
        return PrCommentsResponse.from(comments);
    }

    public PrComment getCommentById(Long id) {
        return prCommentRepository.findById(id).orElseThrow(
            () -> new PrCommentNotExistException("커멘트 정보를 찾을 수 없습니다."));
    }
}
