package io.iktech.jenkins.plugins.artifactz;

import io.iktech.jenkins.plugins.artifactz.client.ServiceClientFactory;
import io.iktech.jenkins.plugins.artifactz.client.impl.ServiceClientFactoryImpl;

import java.util.Map;

public class SingletonStore {
    private static ServiceClientFactory singleton;

    public static ServiceClientFactory getInstance() {
        if (singleton == null) {
            singleton = new ServiceClientFactoryImpl();
        }

        return singleton;
    }

    public static void test(ServiceClientFactory test) {
        singleton = test;
    }
}
