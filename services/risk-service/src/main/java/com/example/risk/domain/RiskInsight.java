package com.example.risk.domain;

import java.time.Instant;

public class RiskInsight {

    private final int score;
    private final String level;
    private final String message;
    private final Instant updatedAt;

    public RiskInsight(int score, String level, String message, Instant updatedAt) {
        this.score = score;
        this.level = level;
        this.message = message;
        this.updatedAt = updatedAt;
    }

    public int getScore() {
        return score;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
