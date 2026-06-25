package com.example.flashcards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * AI-generated content for a card, stored permanently to avoid re-generating.
 * One-to-one with CardEntity (cardId is the PK).
 */
@Entity
@Table(name = "card_ai_content")
public class CardAiContentEntity {

    @Id
    @Column(length = 100)
    private String cardId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "card_id")
    private CardEntity card;

    @Column(columnDefinition = "text")
    private String mnemonic;

    @Column(columnDefinition = "text")
    private String aiExampleDe;

    @Column(columnDefinition = "text")
    private String aiExampleEn;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    // getters / setters

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public CardEntity getCard() { return card; }
    public void setCard(CardEntity card) { this.card = card; }

    public String getMnemonic() { return mnemonic; }
    public void setMnemonic(String mnemonic) { this.mnemonic = mnemonic; }

    public String getAiExampleDe() { return aiExampleDe; }
    public void setAiExampleDe(String aiExampleDe) { this.aiExampleDe = aiExampleDe; }

    public String getAiExampleEn() { return aiExampleEn; }
    public void setAiExampleEn(String aiExampleEn) { this.aiExampleEn = aiExampleEn; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
