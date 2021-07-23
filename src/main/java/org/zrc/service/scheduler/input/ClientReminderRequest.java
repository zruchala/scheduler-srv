package org.zrc.service.scheduler.input;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ClientReminderRequest {
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public ClientReminderRequest setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }
}
