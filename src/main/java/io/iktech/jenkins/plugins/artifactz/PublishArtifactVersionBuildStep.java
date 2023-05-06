package io.iktech.jenkins.plugins.artifactz;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.google.inject.Guice;
import com.google.inject.Injector;
import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;
import io.iktech.jenkins.plugins.artifactz.client.ServiceClientFactory;
import io.iktech.jenkins.plugins.artifactz.modules.ServiceClientFactoryModule;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;

public class PublishArtifactVersionBuildStep extends Builder implements SimpleBuildStep {
    private String token;

    private String name;

    private String description;

    private String type;

    private String groupId;

    private String artifactId;

    private String stage;

    private String flow;

    private String stageDescription;

    private String version;

    private transient ServiceClientFactory serviceClientFactory;

    @DataBoundConstructor
    public PublishArtifactVersionBuildStep(String token,
                                           String name,
                                           String description,
                                           String type,
                                           String groupId,
                                           String artifactId,
                                           String stage,
                                           String flow,
                                           String stageDescription,
                                           String version) {
        this.token = token;
        this.name = name;
        this.description = description;
        this.type = type;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.stage = stage;
        this.flow = flow;
        this.stageDescription = stageDescription;
        this.version = version;
        this.serviceClientFactory = SingletonStore.getInstance();
    }

    public String getToken() {
        return token;
    }

    @DataBoundSetter
    public void setToken(String token) {
        this.token = token;
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
        final EnvVars env = run.getEnvironment(taskListener);
        String expandedStage = env.expand(this.getStage());
        String expandedStageDescription = env.expand(this.getStageDescription());
        String expandedName = env.expand(this.getName());
        String expandedDescription = env.expand(this.getDescription());
        String expandedGroupId = env.expand(this.getGroupId());
        String expandedArtifactId = env.expand(this.getArtifactId());
        String expandedVersion = env.expand(this.getVersion());
        taskListener.getLogger().println("Patching the artifact version details at the stage '" + expandedStage + "' to the Artifactor instance @ " + Configuration.get().getServerUrl());
        taskListener.getLogger().println("Artifact details:");
        taskListener.getLogger().println("  name: " + expandedName);
        if (!StringUtils.isEmpty(expandedDescription)) {
            taskListener.getLogger().println("  description: " + expandedDescription);
        }
        if (!StringUtils.isEmpty(expandedGroupId)) {
            taskListener.getLogger().println("  group Id: " + expandedGroupId);
        }
        if (!StringUtils.isEmpty(expandedArtifactId)) {
            taskListener.getLogger().println("  artifact Id: " + expandedArtifactId);
        }
        taskListener.getLogger().println("  version: " + expandedVersion);

        try {
            ServiceClient client = this.serviceClientFactory.serviceClient(taskListener, ServiceHelper.getToken(run, taskListener, this.token));
            client.publishArtifact(expandedStage, expandedStageDescription, expandedName, expandedDescription, this.getFlow(), this.getType(), expandedGroupId, expandedArtifactId, expandedVersion);
            taskListener.getLogger().println("Successfully patched artifact version");
        } catch (ClientException e) {
            ServiceHelper.interruptExecution(run, taskListener, "Error while publishing artifact version", e);
            throw new AbortException(e.getMessage());
        }
    }

    @Symbol("artifactVersion")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value == null || value.length() == 0) {
                return FormValidation.error(Messages.Artifact_DescriptorImpl_errors_missingName());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckStage(@QueryParameter String value)
                throws IOException, ServletException {
            if (value == null || value.length() == 0) {
                return FormValidation.error(Messages.Artifact_DescriptorImpl_errors_missingStage());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckType(@QueryParameter String value)
                throws IOException, ServletException {
            if (value == null || value.length() == 0) {
                return FormValidation.error(Messages.PublishArtifactVersionBuildStep_DescriptorImpl_errors_missingType());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckVersion(@QueryParameter String value)
                throws IOException, ServletException {
            if (value == null || value.length() == 0) {
                return FormValidation.error(Messages.PublishArtifactVersionBuildStep_DescriptorImpl_errors_missingVersion());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckGroupId(@QueryParameter String value, @QueryParameter String type)
                throws IOException, ServletException {
            if (isJavaArtifact(type) && (value == null || value.length() == 0)) {
                return FormValidation.error(Messages.PublishArtifactVersionBuildStep_DescriptorImpl_errors_missingGroupId());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckArtifactId(@QueryParameter String value, @QueryParameter String type)
                throws IOException, ServletException {
            if (isJavaArtifact(type) && (value == null || value.length() == 0)) {
                return FormValidation.error(Messages.PublishArtifactVersionBuildStep_DescriptorImpl_errors_missingArtifactId());
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
            return Messages.PublishArtifactVersionBuildStep_DescriptorImpl_DisplayName();
        }

        public ListBoxModel doFillTypeItems(@AncestorInPath ItemGroup context) {
            StandardListBoxModel result = new StandardListBoxModel();
            result.includeEmptyValue();
            result.add("JAR Archive", "JAR");
            result.add("WAR Archive", "WAR");
            result.add("EAR Archive", "EAR");
            result.add("Docker Image", "DockerImage");
            return result;
        }
    }
}
