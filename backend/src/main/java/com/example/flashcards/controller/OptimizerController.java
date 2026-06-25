package com.example.flashcards.controller;

import com.example.flashcards.service.FsrsOptimizerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/review/optimizer")
public class OptimizerController {

    private final FsrsOptimizerService optimizerService;

    public OptimizerController(FsrsOptimizerService optimizerService) {
        this.optimizerService = optimizerService;
    }

    @GetMapping
    public FsrsOptimizerService.OptimizerResult analyze(@AuthenticationPrincipal UserDetails principal) {
        return optimizerService.analyze(principal.getUsername());
    }
}
