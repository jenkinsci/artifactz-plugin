package io.iktech.jenkins.plugin.artifactz;

import hudson.model.TaskListener;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;
import io.iktech.jenkins.plugins.artifactz.client.ServiceClientFactory;

import static org.mockito.Mockito.mock;

public class TestServiceClientFactory implements ServiceClientFactory {
    private ServiceClient serviceClient;

    public TestServiceClientFactory() {
        this.serviceClient = mock(ServiceClient.class);
    }

    public ServiceClient getServiceClient() {
        this.serviceClient = mock(ServiceClient.class);
        return this.serviceClient;
    }

    public ServiceClient serviceClient(TaskListener taskListener, String token) throws ClientException {
        return this.serviceClient;
    }
}
