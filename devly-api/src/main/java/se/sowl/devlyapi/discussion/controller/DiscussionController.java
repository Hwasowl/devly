package se.sowl.devlyapi.discussion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.sowl.devlyapi.discussion.controller.dto.DiscussionStartRequest;
import se.sowl.devlyapi.discussion.service.DiscussionService;
import se.sowl.devlyapi.discussion.service.dto.AnswerRequest;
import se.sowl.devlyapi.discussion.service.dto.DiscussionResultResponse;
import se.sowl.devlyapi.discussion.service.dto.DiscussionStartResponse;
import se.sowl.devlyapi.discussion.service.dto.RoundResponse;
import se.sowl.devlydomain.discussion.domain.DiscussionTopic;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {
    private final DiscussionService discussionService;
    
    @GetMapping("/topics")
    public ResponseEntity<List<DiscussionTopic>> getDiscussionTopics(
            @RequestParam(required = false) String category) {
        List<DiscussionTopic> topics = category != null
            ? discussionService.getDiscussionTopicsByCategory(category)
            : discussionService.getDiscussionTopics();
        
        return ResponseEntity.ok(topics);
    }
    
    @PostMapping("/start")
    public ResponseEntity<DiscussionStartResponse> startDiscussion(
            @RequestBody @Valid DiscussionStartRequest request) {
        log.info("Starting discussion for user {} with topic {}", request.getUserId(), request.getTopicId());
        
        DiscussionStartResponse response = discussionService.startDiscussion(
            request.getUserId(), request.getTopicId());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{discussionId}/answer")
    public ResponseEntity<RoundResponse> submitAnswer(
            @PathVariable Long discussionId,
            @RequestBody @Valid AnswerRequest request) {
        log.info("Submitting answer for discussion {} round {}", discussionId, request.getRoundNumber());
        
        RoundResponse response = discussionService.submitAnswer(discussionId, request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{discussionId}/result")
    public ResponseEntity<DiscussionResultResponse> getDiscussionResult(
            @PathVariable Long discussionId) {
        log.info("Getting result for discussion {}", discussionId);
        
        DiscussionResultResponse response = discussionService.getDiscussionResult(discussionId);
        
        return ResponseEntity.ok(response);
    }
}