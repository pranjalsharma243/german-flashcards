package com.example.flashcards.entity;

import com.example.flashcards.srs.FsrsEngine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
    name = "review_logs",
    indexes = {
        @Index(name = "idx_rl_user_reviewed", columnList = "user_id, reviewed_at"),
        @Index(name = "idx_rl_card", columnList = "card_id")
    }
)
public class ReviewLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private CardEntity card;

    /** 1=Again 2=Hard 3=Good 4=Easy */
    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private int elapsedDays;

    @Column(nullable = false)
    private int scheduledDays;

    @Column(nullable = false)
    private double stability;

    @Column(nullable = false)
    private double difficulty;

    @Column(nullable = false)
    private Instant reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FsrsEngine.CardState state;

    // Getters & setters

    public Long getId() { return id; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public CardEntity getCard() { return card; }
    public void setCard(CardEntity card) { this.card = card; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public int getElapsedDays() { return elapsedDays; }
    public void setElapsedDays(int elapsedDays) { this.elapsedDays = elapsedDays; }

    public int getScheduledDays() { return scheduledDays; }
    public void setScheduledDays(int scheduledDays) { this.scheduledDays = scheduledDays; }

    public double getStability() { return stability; }
    public void setStability(double stability) { this.stability = stability; }

    public double getDifficulty() { return difficulty; }
    public void setDifficulty(double difficulty) { this.difficulty = difficulty; }

    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }

    public FsrsEngine.CardState getState() { return state; }
    public void setState(FsrsEngine.CardState state) { this.state = state; }
}
