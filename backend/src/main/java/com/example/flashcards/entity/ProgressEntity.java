package com.example.flashcards.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "chapter_id"})
)
public class ProgressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chapter_id")
    private ChapterEntity chapter;

    @ElementCollection
    @CollectionTable(name = "progress_known_cards", joinColumns = @JoinColumn(name = "progress_id"))
    @Column(name = "card_id", nullable = false)
    private Set<String> knownCardIds = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "progress_practice_cards", joinColumns = @JoinColumn(name = "progress_id"))
    @Column(name = "card_id", nullable = false)
    private Set<String> practiceCardIds = new LinkedHashSet<>();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public ChapterEntity getChapter() {
        return chapter;
    }

    public void setChapter(ChapterEntity chapter) {
        this.chapter = chapter;
    }

    public Set<String> getKnownCardIds() {
        return knownCardIds;
    }

    public void setKnownCardIds(Set<String> knownCardIds) {
        this.knownCardIds = knownCardIds;
    }

    public Set<String> getPracticeCardIds() {
        return practiceCardIds;
    }

    public void setPracticeCardIds(Set<String> practiceCardIds) {
        this.practiceCardIds = practiceCardIds;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
