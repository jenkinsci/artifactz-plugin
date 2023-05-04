package io.iktech.jenkins.plugins.artifactz;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.AbortException;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.ServiceClientBuilder;
import io.artifactz.client.exception.ClientException;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class ServiceHelper {
    public static ServiceClient getClient(String token) throws ClientException {
        return getClient(null, token);
    }

    public static void interruptExecution(@Nonnull Run<?, ?> run, @Nonnull TaskListener taskListener, String message) {
        taskListener.fatalError(message);
        Executor executor = run.getExecutor();
        if (executor != null) {
            executor.interrupt(Result.FAILURE);
        }
    }

    public static ServiceClient getClient(TaskListener taskListener, String token) throws ClientException {
        String proxyUsername = null;
        String proxyPassword = null;

        StandardUsernamePasswordCredentials proxyCredentials = getProxyCredentials();
        if (proxyCredentials != null) {
            proxyUsername = proxyCredentials.getUsername();
            proxyPassword = proxyCredentials.getPassword().getPlainText();
        }

        ServiceClientBuilder builder = ServiceClientBuilder
                .withBaseUrl(Configuration.get().getServerUrl())
                .withApiToken(token)
                .withUserAgent("Jenkins Artifactz.io plugin/1.0")
                .withSender("jenkins-plugin")
                .withProxyUrl(Configuration.get().getProxy())
                .withProxyUsername(proxyUsername)
                .withProxyPassword(proxyPassword)
                .provideFeedback(taskListener != null ? new FeedbackImpl(taskListener) : null);

        return builder.build();
    }

    public static StandardUsernamePasswordCredentials getProxyCredentials() {
        if (!StringUtils.isEmpty(Configuration.get().getProxyCredentialsId())) {
            return CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(
                            StandardUsernamePasswordCredentials.class,
                            Jenkins.get(),
                            ACL.SYSTEM,
                            Collections.emptyList()
                    ), CredentialsMatchers.withId(Configuration.get().getProxyCredentialsId()));
        }

        return null;
    }

    public static HttpHost getProxyHost() throws MalformedURLException  {
        String proxySchema;
        String proxyHost;
        int proxyPort;
        HttpHost proxyHttpHost = null;

        HttpClientBuilder clientbuilder = HttpClients.custom();

        if (!StringUtils.isEmpty(Configuration.get().getProxy())) {
            URL proxyUri = new URL(Configuration.get().getProxy());
            proxySchema = proxyUri.getProtocol();
            proxyHost = proxyUri.getHost();
            proxyPort = proxyUri.getPort();
            if (proxyPort == -1) {
                proxyPort = proxyUri.getDefaultPort();
            }
            proxyHttpHost = new HttpHost(proxyHost, proxyPort, proxySchema);

            StandardUsernamePasswordCredentials proxyCredentials = getProxyCredentials();
            if (proxyCredentials != null) {
                org.apache.http.client.CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(proxyHttpHost),
                        new UsernamePasswordCredentials(proxyCredentials.getUsername(), proxyCredentials.getPassword().getPlainText()));
                clientbuilder.setDefaultCredentialsProvider(credsProvider);
            }
        }

        return proxyHttpHost;
    }

    public static String getToken(Run<?, ?> run, TaskListener taskListener, String inlineToken) throws AbortException {
        if (inlineToken == null) {
            String credentialsId = Configuration.get().getCredentialsId();
            if (credentialsId == null) {
                ServiceHelper.interruptExecution(run, taskListener, "Artifactz access credentials are not defined. Cannot continue.");
                throw new AbortException("Artifactz access credentials are not defined. Cannot continue.");
            }

            StringCredentials token = CredentialsProvider.findCredentialById(credentialsId, StringCredentials.class, run);
            if (token == null) {
                ServiceHelper.interruptExecution(run, taskListener, "Could not find specified credentials. Cannot continue.");
                throw new AbortException("Could not find specified credentials. Cannot continue.");
            }

            return token.getSecret().getPlainText();
        }

        return inlineToken;
    }
}
