package io.iktech.jenkins.plugins.artifactor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iktech.jenkins.plugins.artifactor.model.ErrorMessage;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class RequestHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String retrieveVersion(String token, String stage, String artifactsQuery) throws ExchangeException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet getVersion = new HttpGet(Configuration.get().getServerUrl() + "/stages/" + stage + "/list?" + artifactsQuery);
        getVersion.setHeader("Accepts", ContentType.APPLICATION_JSON.getMimeType());
        getVersion.setHeader("Authorization", "Bearer " + token);
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
}
