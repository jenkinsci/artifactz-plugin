package io.iktech.jenkins.plugins.artifactor;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.google.common.collect.ImmutableSet;
import hudson.*;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;
import io.artifactz.client.model.Version;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RetrieveArtifactsStep extends Step {
    private static Logger logger = LoggerFactory.getLogger(RetrieveArtifactsStep.class);
    private String stage;
    private List<String> names;

    @DataBoundConstructor
    public RetrieveArtifactsStep(String stage, List<String> names) {
        this.stage = stage;
        this.names = names;
    }

    public String getStage() {
        return stage;
    }

    @DataBoundSetter
    public void setStage(String stage) {
        this.stage = stage;
    }

    public List<String> getNames() {
        return names;
    }

    @DataBoundSetter
    public void setNames(List<String> names) {
        this.names = names;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(stage, names, context);
    }

    private static final class Execution extends SynchronousNonBlockingStepExecution<Map<String, String>> {
        private static final long serialVersionUID = 6190377462479580850L;

        private final String stage;
        private final List<String> names;

        Execution(String stage, List<String> names, StepContext context) {
            super(context);
            this.stage = stage;
            this.names = names;
        }

        @Override protected Map<String, String> run() throws Exception {
            Run<?, ?> run = getContext().get(Run.class);
            TaskListener taskListener = getContext().get(TaskListener.class);

            assert taskListener != null;
            PrintStream l = taskListener.getLogger();
            l.println("Retrieving versions of the following artifacts at the stage '" + this.stage + "'");

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

            try {
                ServiceClient client = ServiceHelper.getClient(taskListener, token.getSecret().getPlainText());
                io.artifactz.client.model.Stage stage = client.retrieveVersions(this.stage, this.names.toArray(new String[0]));
                logger.info("Content has been converted to the object");
                if (stage.getArtifacts() != null) {
                    l.println("Successfully retrieved artifact versions");
                    return stage.getArtifacts().stream().collect(Collectors.toMap(Version::getArtifactName, Version::getVersion));
                }
                String errorMessage = "No artifacts data in the response";
                logger.info("Service returned empty result set");
                ServiceHelper.interruptExecution(run, taskListener, errorMessage);
                throw new AbortException(errorMessage);
            } catch (ClientException e) {
                logger.error("Error while retrieving artifact versions", e);
                String errorMessage = "Error while retrieving artifact versions: " + e.getMessage();
                ServiceHelper.interruptExecution(run, taskListener, errorMessage);
                throw new AbortException(errorMessage);
            }
        }
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {
        @Override
        public String getFunctionName() {
            return "retrieveArtifacts";
        }

        @Override
        public String getDisplayName() {
            return "Retrieve Artifact Versions";
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
