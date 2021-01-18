package io.iktech.jenkins.plugins.artifactor;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.security.ACL;
import io.iktech.jenkins.plugins.artifactor.model.ErrorMessage;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;

public class RequestHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String retrieveVersion(String token, String stage, String artifactsQuery) throws ExchangeException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet getVersion = new HttpGet(Configuration.get().getServerUrl() + "/stages/" + stage + "/list?" + artifactsQuery);
        HttpHost proxyHttpHost;

        try {
            proxyHttpHost = getProxyHost();
        } catch (MalformedURLException e) {
            throw new ExchangeException("Incorrect proxy URL specified: " + Configuration.get().getProxy());
        }

        if (proxyHttpHost != null) {
            RequestConfig.Builder reqconfigconbuilder = RequestConfig.custom();
            reqconfigconbuilder = reqconfigconbuilder.setProxy(proxyHttpHost);
            RequestConfig config = reqconfigconbuilder.build();
            getVersion.setConfig(config);
        }

        getVersion.setHeader("Accepts", ContentType.APPLICATION_JSON.getMimeType());
        getVersion.setHeader("Authorization", "Bearer " + token);
        if (Configuration.get().getSender() != null) {
            getVersion.setHeader("X-ClientId", Configuration.get().getSender());
        }
        CloseableHttpResponse response = null;
        try {
            response = client.execute(getVersion);
            if (response.getStatusLine().getStatusCode() != 200) {
                String contenType = response.getEntity().getContentType().toString();
                if (StringUtils.equals(contenType, ContentType.APPLICATION_JSON.getMimeType())) {
                    ErrorMessage message = objectMapper.readValue(EntityUtils.toString(response.getEntity()), ErrorMessage.class);
                    throw new ExchangeException(message.getError());
                } else {
                    throw new ExchangeException("Unknown error");
                }
            } else {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            throw new ExchangeException("Failed to get data from the Artifactor Server", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {}
            }

            try {
                client.close();
            } catch (IOException e) {}
        }
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

            if (!StringUtils.isEmpty(Configuration.get().getProxyCredentialsId())) {
                StandardUsernamePasswordCredentials proxyCredentials = CredentialsMatchers.firstOrNull(
                        CredentialsProvider.lookupCredentials(
                                StandardUsernamePasswordCredentials.class,
                                Jenkins.get(),
                                ACL.SYSTEM,
                                Collections.emptyList()
                        ), CredentialsMatchers.withId(Configuration.get().getProxyCredentialsId()));

                org.apache.http.client.CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(proxyHttpHost),
                        new UsernamePasswordCredentials(proxyCredentials.getUsername(), proxyCredentials.getPassword().getPlainText()));
                clientbuilder.setDefaultCredentialsProvider(credsProvider);
            }
        }

        return proxyHttpHost;
    }
}
