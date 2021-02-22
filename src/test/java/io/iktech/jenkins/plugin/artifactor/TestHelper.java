package io.iktech.jenkins.plugin.artifactor;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.util.Secret;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.ServiceClientBuilder;
import io.artifactz.client.exception.ClientException;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jvnet.hudson.test.JenkinsRule;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class TestHelper {
    public static ServiceClient setupClient() throws ClientException {
        ServiceClient serviceClient = mock(ServiceClient.class);
        ServiceClientBuilder serviceClientBuilder = mock(ServiceClientBuilder.class);

        PowerMockito.mockStatic(ServiceClientBuilder.class);
        when(ServiceClientBuilder.withBaseUrl(any())).thenReturn(serviceClientBuilder);
        when(serviceClientBuilder.withApiToken(any())).thenReturn(serviceClientBuilder);
        when(serviceClientBuilder.withSender(any())).thenReturn(serviceClientBuilder);
        when(serviceClientBuilder.withProxyUrl(any())).thenReturn(serviceClientBuilder);
        when(serviceClientBuilder.withProxyUsername(any())).thenReturn(serviceClientBuilder);
        when(serviceClientBuilder.withProxyPassword(any())).thenReturn(serviceClientBuilder);
        when(serviceClientBuilder.withUserAgent(any())).thenReturn(serviceClientBuilder);
        when(serviceClientBuilder.provideFeedback(any())).thenReturn(serviceClientBuilder);
        when(serviceClientBuilder.build()).thenReturn(serviceClient);

        return serviceClient;
    }

    public static void addCredential(JenkinsRule j, String description) throws IOException {
        StringCredentialsImpl c = new StringCredentialsImpl(CredentialsScope.USER, "test", description, Secret.fromString("value"));
        CredentialsProvider.lookupStores(j).iterator().next().addCredentials(Domain.global(), c);
        UsernamePasswordCredentialsImpl up = new UsernamePasswordCredentialsImpl(CredentialsScope.USER, "proxy-test", "proxy" + description, "user",  "value");
        CredentialsProvider.lookupStores(j).iterator().next().addCredentials(Domain.global(), up);
    }

}
