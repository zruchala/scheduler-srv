package com.flexvoucher.scheduler.input;

import java.time.LocalDateTime;

public class ClientCreated {

    private String clientId;
    private LocalDateTime createdAt;

    public ClientCreated() {
        this.createdAt = LocalDateTime.now();
    }

    public ClientCreated setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientId() {
        return this.clientId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ClientCreated setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
