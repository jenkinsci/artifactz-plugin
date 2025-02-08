package io.iktech.jenkins.plugin.artifactz;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Descriptor;
import hudson.util.Secret;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.ServiceClientBuilder;
import io.artifactz.client.exception.ClientException;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestHelper {
    public static void addCredential(JenkinsRule j, String description) throws IOException {
        StringCredentialsImpl c = new StringCredentialsImpl(CredentialsScope.USER, "test", description, Secret.fromString("value"));
        CredentialsProvider.lookupStores(j).iterator().next().addCredentials(Domain.global(), c);
        try {
            UsernamePasswordCredentialsImpl up = new UsernamePasswordCredentialsImpl(CredentialsScope.USER, "proxy-test", "proxy" + description, "user", "value");
            CredentialsProvider.lookupStores(j).iterator().next().addCredentials(Domain.global(), up);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
