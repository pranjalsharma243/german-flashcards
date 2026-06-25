package com.example.flashcards.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "tts_cache")
public class TtsCacheEntity {

    /** Keyed by a stable hash of the spoken text + voice */
    @Id
    @Column(length = 100)
    private String cacheKey;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] audioMp3;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public String getCacheKey() { return cacheKey; }
    public void setCacheKey(String cacheKey) { this.cacheKey = cacheKey; }

    public byte[] getAudioMp3() { return audioMp3; }
    public void setAudioMp3(byte[] audioMp3) { this.audioMp3 = audioMp3; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
