package com.example.core.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
public class Txn {
    @Id
    private String id = UUID.randomUUID().toString();

    // Stores masked card number (first 4 + ******** + last 4) for security
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    private TxnType type;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TxnStatus status;

    private String declineReason;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    private BigDecimal balanceAfter;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public TxnType getType() { return type; }
    public void setType(TxnType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TxnStatus getStatus() { return status; }
    public void setStatus(TxnStatus status) { this.status = status; }

    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
}
