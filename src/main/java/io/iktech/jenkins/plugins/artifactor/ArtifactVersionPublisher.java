package io.iktech.jenkins.plugins.artifactor;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.iktech.jenkins.plugins.artifactor.model.ErrorMessage;
import io.iktech.jenkins.plugins.artifactor.model.PatchVersionRequest;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;

public class ArtifactVersionPublisher extends Builder implements SimpleBuildStep {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String name;
    private String description;
    private String type;
    private String groupId;
    private String artifactId;
    private String stage;
    private String flow;
    private String stageDescription;
    private String version;

    @DataBoundConstructor
    public ArtifactVersionPublisher(String name,
                                    String description,
                                    String type,
                                    String groupId,
                                    String artifactId,
                                    String stage,
                                    String flow,
                                    String stageDescription,
                                    String version) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.stage = stage;
        this.flow = flow;
        this.stageDescription = stageDescription;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    @DataBoundSetter
    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    @DataBoundSetter
    public void setType(String type) {
        this.type = type;
    }

    public String getGroupId() {
        return groupId;
    }

    @DataBoundSetter
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @DataBoundSetter
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getStage() {
        return stage;
    }

    @DataBoundSetter
    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getFlow() {
        return flow;
    }

    @DataBoundSetter
    public void setFlow(String flow) {
        this.flow = flow;
    }

    public String getVersion() {
        return version;
    }

    public String getStageDescription() {
        return stageDescription;
    }

    @DataBoundSetter
    public void setStageDescription(String stageDescription) {
        this.stageDescription = stageDescription;
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
        stageDescription = env.expand(this.stageDescription);
        name = env.expand(this.name);
        description = env.expand(this.description);
        groupId = env.expand(this.groupId);
        artifactId = env.expand(this.artifactId);
        version = env.expand(this.version);
        taskListener.getLogger().println("Patching the artifact version details at the stage '" + stage + "' to the Artifactor instance @ " + Configuration.get().getServerUrl());
        taskListener.getLogger().println("Artifact details:");
        taskListener.getLogger().println("  name: " + name);
        if (!StringUtils.isEmpty(description)) {
            taskListener.getLogger().println("  description: " + description);
        }
        if (!StringUtils.isEmpty(groupId)) {
            taskListener.getLogger().println("  group Id: " + groupId);
        }
        if (!StringUtils.isEmpty(artifactId)) {
            taskListener.getLogger().println("  artifact Id: " + artifactId);
        }
        taskListener.getLogger().println("  version: " + version);
        PatchVersionRequest request = new PatchVersionRequest();
        request.setStage(stage);
        request.setFlow(flow);
        request.setStageDescription(stageDescription);
        request.setName(name);
        request.setDescription(description);
        request.setType(type);
        request.setGroupId(groupId);
        request.setArtifactId(artifactId);
        request.setVersion(version);
        CloseableHttpClient client = HttpClients.createDefault();

        HttpPut patch = new HttpPut(Configuration.get().getServerUrl() + "/artifacts/versions");

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
                taskListener.fatalError("Error while patching artifact version: " + message.getError());
            } else {
                taskListener.fatalError("Error while patching artifact version");
            }
            run.getExecutor().interrupt(Result.FAILURE);
        } else {
            taskListener.getLogger().println("Successfully patched artifact version");
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

        public FormValidation doCheckType(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error(Messages.ArtifactVersionPublisher_DescriptorImpl_errors_missingType());
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

        public FormValidation doCheckGroupId(@QueryParameter String value, @QueryParameter String type)
                throws IOException, ServletException {
            if (isJavaArtifact(type)  && value.length() == 0) {
                return FormValidation.error(Messages.ArtifactVersionPublisher_DescriptorImpl_errors_missingGroupId());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckArtifactId(@QueryParameter String value, @QueryParameter String type)
                throws IOException, ServletException {
            if (isJavaArtifact(type) && value.length() == 0) {
                return FormValidation.error(Messages.ArtifactVersionPublisher_DescriptorImpl_errors_missingArtifactId());
            }
            return FormValidation.ok();
        }

        private boolean isJavaArtifact(String type) {
            return type.equals("JAR") ||
                    type.equals("EAR") ||
                    type.equals("WAR");
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.ArtifactVersionPublisher_DescriptorImpl_DisplayName();
        }

        public ListBoxModel doFillTypeItems(@AncestorInPath ItemGroup context) {
            StandardListBoxModel result = new StandardListBoxModel();
            result.includeEmptyValue();
            result.add("Jar Archive", "JAR");
            result.add("War Archive", "WAR");
            result.add("EAR Archive", "EAR");
            result.add("Docker Image", "DockerImage");
            return result;
        }
    }
}
