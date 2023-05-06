package io.iktech.jenkins.plugins.artifactz;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;
import io.iktech.jenkins.plugins.artifactz.client.ServiceClientFactory;
import io.iktech.jenkins.plugins.artifactz.modules.ServiceClientFactoryModule;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;

public class PushArtifactVersionBuildStep extends Builder implements SimpleBuildStep {
    private String token;

    private String name;

    private String stage;

    private String version;

    private String variableName;

    private transient ServiceClientFactory serviceClientFactory;

    @DataBoundConstructor
    public PushArtifactVersionBuildStep(String token,
                                        String name,
                                        String stage,
                                        String version,
                                        String variableName) {
        this.token = token;
        this.name = name;
        this.stage = stage;
        this.version = version;
        this.variableName = variableName;
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

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        final EnvVars env = run.getEnvironment(taskListener);
        String expandedStage = env.expand(this.getStage());
        String expandedName = env.expand(this.getName());
        String expandedVersion = env.expand(this.getVersion());

        taskListener.getLogger().println("Pushing the artifact version '" + expandedVersion + "' at the stage '" + expandedStage + "'");
        taskListener.getLogger().println("Performing PUT request to  to the Artifactor instance @" + Configuration.get().getServerUrl());
        taskListener.getLogger().println("Artifact details:");
        taskListener.getLogger().println("  name: " + expandedName);
        taskListener.getLogger().println("  stage: " + expandedStage);
        taskListener.getLogger().println("  version: " + expandedVersion);

        try {
            ServiceClient client = this.serviceClientFactory.serviceClient(taskListener, ServiceHelper.getToken(run, taskListener, this.token));
            String pushedVersion;
            if (!StringUtils.isEmpty(expandedVersion)) {
                pushedVersion = client.pushArtifact(expandedStage, expandedName, expandedVersion);
            } else {
                pushedVersion = client.pushArtifact(expandedStage, expandedName);
            }
            String variableName = !StringUtils.isEmpty(this.getVariableName()) ? this.getVariableName() : "ARTIFACTZ_VERSION";
            run.addAction(new InjectVariable(variableName, pushedVersion));
            taskListener.getLogger().println("Successfully pushed artifact version");
        } catch (ClientException e) {
            ServiceHelper.interruptExecution(run, taskListener, "Error while pushing artifact version", e);
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

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.PushArtifactVersionBuildStep_DescriptorImpl_DisplayName();
        }
    }
}
