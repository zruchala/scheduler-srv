package com.flexvoucher.scheduler.input;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ClientDeletionRequest {
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public ClientDeletionRequest setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

}
