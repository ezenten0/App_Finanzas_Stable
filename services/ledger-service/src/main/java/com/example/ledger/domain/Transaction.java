package com.example.ledger.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class Transaction {

    private String id;
    private String userId;
    private TransactionType type;
    private BigDecimal amount;
    private String title;
    private String description;
    private String category;
    private LocalDate eventDate;
    private TransactionStatus status;
    private Instant createdAt;
    private String currency;

    public Transaction() {
    }

    public Transaction(String id, String userId, TransactionType type, BigDecimal amount, String title,
                       String description, String category, LocalDate eventDate, TransactionStatus status,
                       String currency, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.title = title;
        this.description = description;
        this.category = category;
        this.eventDate = eventDate;
        this.status = status;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
