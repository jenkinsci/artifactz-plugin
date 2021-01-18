package io.iktech.jenkins.plugins.artifactor;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.iktech.jenkins.plugins.artifactor.model.ErrorMessage;
import io.iktech.jenkins.plugins.artifactor.model.PushVersionRequest;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;

public class ArtifactVersionPusher extends Builder implements SimpleBuildStep {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String name;
    private String stage;
    private String version;

    @DataBoundConstructor
    public ArtifactVersionPusher(String name,
                                 String stage,
                                 String version) {
        this.name = name;
        this.stage = stage;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    public String getStage() {
        return stage;
    }

    @DataBoundSetter
    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getVersion() {
        return version;
    }

    @DataBoundSetter
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        HttpHost proxyHttpHost = null;

        try {
            proxyHttpHost = RequestHelper.getProxyHost();
        } catch (MalformedURLException e) {
            taskListener.getLogger().println("Incorrect proxy URL specified: " + Configuration.get().getProxy() + ". Ignoring...");
        }

        final EnvVars env = run.getEnvironment(taskListener);
        stage = env.expand(this.stage);
        name = env.expand(this.name);
        version = env.expand(this.version);
        taskListener.getLogger().println("Pushing the artifact version '" + version + "' at the stage '" + stage + "'");
        taskListener.getLogger().println("Performing PUT request to  to the Artifactor instance @" + Configuration.get().getServerUrl());
        taskListener.getLogger().println("Artifact details:");
        taskListener.getLogger().println("  name: " + name);
        taskListener.getLogger().println("  stage: " + stage);
        taskListener.getLogger().println("  version: " + version);
        PushVersionRequest request = new PushVersionRequest();
        request.setStage(stage);
        request.setName(name);
        request.setVersion(version);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut patch = new HttpPut(Configuration.get().getServerUrl() + "/artifacts/push");
        if (proxyHttpHost != null) {
            RequestConfig.Builder reqconfigconbuilder = RequestConfig.custom();
            reqconfigconbuilder = reqconfigconbuilder.setProxy(proxyHttpHost);
            RequestConfig config = reqconfigconbuilder.build();
            patch.setConfig(config);
        }

        String content = objectMapper.writeValueAsString(request);
        StringEntity entity = new StringEntity(content, ContentType.APPLICATION_JSON);
        patch.setEntity(entity);
        patch.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        patch.setHeader("Accepts", ContentType.APPLICATION_JSON.getMimeType());
        if (Configuration.get().getSender() != null) {
            patch.setHeader("X-ClientId", Configuration.get().getSender());
        }
        StringCredentials token = CredentialsProvider.findCredentialById(Configuration.get().getCredentialsId(), StringCredentials.class, run);
        patch.setHeader("Authorization", "Bearer " + token.getSecret().getPlainText());
        CloseableHttpResponse response = client.execute(patch);
        if (response.getStatusLine().getStatusCode() != 202) {
            String contenType = response.getEntity().getContentType().toString();
            taskListener.getLogger().println("Content Type: " + contenType);
            if (StringUtils.equals(contenType, ContentType.APPLICATION_JSON.getMimeType())) {
                ErrorMessage message = objectMapper.readValue(EntityUtils.toString(response.getEntity()), ErrorMessage.class);
                taskListener.fatalError("Error while pushing artifact version: " + message.getError());
            } else {
                taskListener.fatalError("Error while pushing artifact version");
            }
            run.getExecutor().interrupt(Result.FAILURE);
        } else {
            taskListener.getLogger().println("Successfully pushed artifact version");
        }
        response.close();
        client.close();
    }

    @Symbol("artifactVersion")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error(Messages.Artifact_DescriptorImpl_errors_missingName());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckStage(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error(Messages.Artifact_DescriptorImpl_errors_missingStage());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckVersion(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error(Messages.ArtifactVersionPublisher_DescriptorImpl_errors_missingVersion());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.ArtifactVersionPusher_DescriptorImpl_DisplayName();
        }
    }
}
