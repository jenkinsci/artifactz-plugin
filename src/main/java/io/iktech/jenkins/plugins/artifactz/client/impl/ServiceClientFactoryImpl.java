package io.iktech.jenkins.plugins.artifactz.client.impl;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.inject.Provides;
import hudson.model.TaskListener;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.ServiceClientBuilder;
import io.artifactz.client.exception.ClientException;
import io.iktech.jenkins.plugins.artifactz.Configuration;
import io.iktech.jenkins.plugins.artifactz.FeedbackImpl;
import io.iktech.jenkins.plugins.artifactz.ServiceHelper;
import io.iktech.jenkins.plugins.artifactz.client.ServiceClientFactory;

public class ServiceClientFactoryImpl implements ServiceClientFactory {
    @Provides
    public ServiceClient serviceClient(TaskListener taskListener, String token) throws ClientException {
        String proxyUsername = null;
        String proxyPassword = null;

        StandardUsernamePasswordCredentials proxyCredentials = ServiceHelper.getProxyCredentials();
        if (proxyCredentials != null) {
            proxyUsername = proxyCredentials.getUsername();
            proxyPassword = proxyCredentials.getPassword().getPlainText();
        }

        ServiceClientBuilder builder = new ServiceClientBuilder(Configuration.get().getServerUrl(), token)
                .withUserAgent("Jenkins Artifactz.io plugin/1.0")
                .withSender("jenkins-plugin")
                .withProxyUrl(Configuration.get().getProxy())
                .withProxyUsername(proxyUsername)
                .withProxyPassword(proxyPassword)
                .provideFeedback(taskListener != null ? new FeedbackImpl(taskListener) : null);

        return builder.build();
    }
}
