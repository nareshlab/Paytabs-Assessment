package com.example.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import java.math.BigDecimal;

/**
 * Card entity stores only hashed card numbers (SHA-256) as the primary key to satisfy
 * the "cryptography for card storage" requirement. Plain card numbers are NEVER persisted.
 */
@Entity
public class Card {
    @Id
    private String cardHash; // SHA-256 hash of the real card number (acts as PK)

    private String pinHash; // SHA-256 hash of PIN
    private BigDecimal balance;

    @Transient
    private String plainCardNumber; // used transiently when responding (not stored)

    public String getCardHash() { return cardHash; }
    public void setCardHash(String cardHash) { this.cardHash = cardHash; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getPlainCardNumber() { return plainCardNumber; }
    public void setPlainCardNumber(String plainCardNumber) { this.plainCardNumber = plainCardNumber; }
}
