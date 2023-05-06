package io.iktech.jenkins.plugins.artifactz.client;

import hudson.ExtensionPoint;
import hudson.model.TaskListener;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;

public interface ServiceClientFactory {
    ServiceClient serviceClient(TaskListener taskListener, String token) throws ClientException;
}
