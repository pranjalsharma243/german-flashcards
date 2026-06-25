package com.example.flashcards.controller;

import com.example.flashcards.model.ReviewQueueItem;
import com.example.flashcards.model.ReviewRequest;
import com.example.flashcards.model.ReviewSubmitResponse;
import com.example.flashcards.model.StatsResponse;
import com.example.flashcards.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * GET /api/review/queue?chapterId=ch-01&limit=20
     * Returns cards due for review (mix of overdue + new).
     */
    @GetMapping("/queue")
    public List<ReviewQueueItem> queue(
            @RequestParam(required = false) String chapterId,
            @RequestParam(defaultValue = "20") int limit,
            Principal principal
    ) {
        limit = Math.min(50, Math.max(1, limit));
        return reviewService.getQueue(principal.getName(), chapterId, limit);
    }

    /**
     * POST /api/review/{cardId}
     * Body: { "rating": 1|2|3|4 }  (1=Again 2=Hard 3=Good 4=Easy)
     */
    @PostMapping("/{cardId}")
    public ReviewSubmitResponse submitReview(
            @PathVariable String cardId,
            @Valid @RequestBody ReviewRequest request,
            Principal principal
    ) {
        return reviewService.submitReview(principal.getName(), cardId, request.rating());
    }

    /** GET /api/review/stats — dashboard stats */
    @GetMapping("/stats")
    public StatsResponse stats(Principal principal) {
        return reviewService.getStats(principal.getName());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.notFound().build();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
