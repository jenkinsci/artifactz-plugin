package io.iktech.jenkins.plugins.artifactz;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.google.common.collect.ImmutableSet;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Set;

public class PushArtifactStep extends Step {
    private static final Logger logger = LoggerFactory.getLogger(PushArtifactStep.class);
    private String token;

    private String stage;

    private String name;

    private String version;

    @DataBoundConstructor
    public PushArtifactStep(String token, String stage, String name, String version, String variable) {
        this.token = token;
        this.stage = stage;
        this.name = name;
        this.version = version;
    }

    public String getToken() {
        return token;
    }

    @DataBoundSetter
    public void setToken(String token) {
        this.token = token;
    }

    public String getStage() {
        return stage;
    }

    @DataBoundSetter
    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    @DataBoundSetter
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new PushArtifactStep.Execution(this.token, this.stage, this.name, this.version, context);
    }

    private static final class Execution extends SynchronousNonBlockingStepExecution<String> {
        private static final long serialVersionUID = 7351377150717079126L;

        private final String token;

        private final String stage;

        private final String name;

        private final String version;

        Execution(String token, String stage, String name, String version, StepContext context) {
            super(context);
            this.token = token;
            this.stage = stage;
            this.name = name;
            this.version = version;
        }

        @Override protected String run() throws Exception {
            Run<?, ?> run = getContext().get(Run.class);
            TaskListener taskListener = getContext().get(TaskListener.class);

            PrintStream l = taskListener.getLogger();
            l.println("Pushing artifact '" + this.name + "' at the stage '" + this.stage + "'");

            try {
                ServiceClient client = ServiceHelper.getClient(taskListener, ServiceHelper.getToken(run, taskListener, this.token));
                String v = client.pushArtifact(this.stage, this.name, this.version);
                taskListener.getLogger().println("Successfully pushed artifact versions");
                return v;
            } catch (ClientException e) {
                logger.error("Error while pushing artifact version", e);
                String errorMessage = "Error while pushing artifact version: " + e.getMessage();
                ServiceHelper.interruptExecution(run, taskListener, errorMessage);
                throw new AbortException(errorMessage);
            }
        }
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {
        @Override
        public String getFunctionName() {
            return "pushArtifact";
        }

        @Override
        public String getDisplayName() {
            return "Push Artifact Version";
        }

        @Override
        public boolean isMetaStep() {
            return false;
        }

        @Override public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }
    }
}
