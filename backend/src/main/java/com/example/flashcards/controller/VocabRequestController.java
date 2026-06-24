package com.example.flashcards.controller;

import com.example.flashcards.model.VocabRequestDto;
import com.example.flashcards.service.VocabRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vocab-requests")
public class VocabRequestController {

    private final VocabRequestService service;

    public VocabRequestController(VocabRequestService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public VocabRequestDto upload(@RequestParam("file") MultipartFile file, Principal principal) {
        try {
            return service.submitFromFile(file, principal.getName());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/mine")
    public List<VocabRequestDto> myRequests(Principal principal) {
        return service.getMyRequests(principal.getName());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
        return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
}
