package com.github.addisonlee.appconfig.testclasses;

import com.github.addisonlee.appconfig.ACAListener;

public class ClientConfigurationACAListener implements ACAListener {
    private ClientConfiguration clientConfiguration;

    public ClientConfigurationACAListener(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }

    @Override
    public void updateConfig(String configValue) {
        clientConfiguration.setValue(configValue);
    }
}
