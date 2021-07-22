package com.flexvoucher.scheduler.input;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ClientConfirmed {
    private String clientId;

    public String getClientId() {
        return clientId;
    }
    public ClientConfirmed setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }
}
