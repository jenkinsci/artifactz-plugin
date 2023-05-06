package io.iktech.jenkins.plugins.artifactz.modules;

import io.iktech.jenkins.plugins.artifactz.client.ServiceClientFactory;
import io.iktech.jenkins.plugins.artifactz.client.impl.ServiceClientFactoryImpl;

public class ServiceClientFactoryModule extends com.google.inject.AbstractModule {
    @Override
    public void configure() {
        bind(ServiceClientFactory.class).to(ServiceClientFactoryImpl.class);
    }
}
