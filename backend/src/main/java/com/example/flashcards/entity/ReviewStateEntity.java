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
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
    name = "review_states",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "card_id"}),
    indexes = {
        @Index(name = "idx_rs_user_due", columnList = "user_id, due_at"),
        @Index(name = "idx_rs_user_card", columnList = "user_id, card_id")
    }
)
public class ReviewStateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private CardEntity card;

    @Column(nullable = false)
    private double stability;

    @Column(nullable = false)
    private double difficulty;

    @Column(nullable = false)
    private int reps;

    @Column(nullable = false)
    private int lapses;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FsrsEngine.CardState state;

    @Column(nullable = false)
    private Instant dueAt;

    @Column(nullable = false)
    private Instant lastReviewedAt;

    // Getters & setters

    public Long getId() { return id; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public CardEntity getCard() { return card; }
    public void setCard(CardEntity card) { this.card = card; }

    public double getStability() { return stability; }
    public void setStability(double stability) { this.stability = stability; }

    public double getDifficulty() { return difficulty; }
    public void setDifficulty(double difficulty) { this.difficulty = difficulty; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public int getLapses() { return lapses; }
    public void setLapses(int lapses) { this.lapses = lapses; }

    public FsrsEngine.CardState getState() { return state; }
    public void setState(FsrsEngine.CardState state) { this.state = state; }

    public Instant getDueAt() { return dueAt; }
    public void setDueAt(Instant dueAt) { this.dueAt = dueAt; }

    public Instant getLastReviewedAt() { return lastReviewedAt; }
    public void setLastReviewedAt(Instant lastReviewedAt) { this.lastReviewedAt = lastReviewedAt; }
}
